from fastapi import FastAPI, APIRouter
from functools import partial
from opentelemetry.instrumentation.asyncpg import AsyncPGInstrumentor
from vyper import v


from .controller import health, user
from .middleware import telemetry
from ..bootstrap import database
from ..bootstrap.logger import logger
from ..bootstrap.tracer import tracer_create
from ..utils.httpclient import HttpClient


def routes():
    router = APIRouter()
    router.include_router(health.router, prefix="/health")
    router.include_router(user.router, prefix="/user")
    return router


async def on_startup(app: FastAPI):
    logger.info("startup", message="server is bootstraping")

    #  opentelemtry
    app.state.tracer = tracer_create(
        v.get("tracer.name"), v.get("tracer.agent"), v.get("tracer.collector")
    )
    AsyncPGInstrumentor().instrument()

    # database
    app.state.dbw = await database.pool(v.get("database.connection.write"))
    app.state.dbr = await database.pool(v.get("database.connection.read"))

    # httpclient
    HttpClient.instance()

    logger.info("startup", message="server is successfully bootstraped")


async def on_shutdown(app: FastAPI):
    logger.info("shutdown", message="server is shutting down")

    # database
    await app.state.dbw.close()
    await app.state.dbr.close()

    # httpclient
    await HttpClient.close()

    logger.info("shutdown", message="server is successfully shut down")


def get_fast() -> FastAPI:
    fast = FastAPI()

    # middleware
    fast.add_middleware(telemetry.HttpTrace)

    # router
    fast.include_router(routes())

    # hooks startup and shutdown handler
    fast.add_event_handler("startup", func=partial(on_startup, app=fast))
    fast.add_event_handler("shutdown", func=partial(on_shutdown, app=fast))

    return fast


api = get_fast()
