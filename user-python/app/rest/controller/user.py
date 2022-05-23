from app.models.user import User
from app.repositories import user_repository
from app.bootstrap.logger import logger
from app.utils.httpclient import HttpClient
from fastapi import APIRouter, Response, Request
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from opentelemetry import trace


router = APIRouter()


@router.get("/")
async def index(request: Request):
    tracer: trace.Tracer = request.app.state.tracer
    try:
        async with HttpClient.instance().get(
            "http://localhost:8080/api/v1/products"
        ) as response:
            if response.status != 200:
                logger.info("httpclient", message="httpclient is not healthy")
            else:
                logger.info("httpclient", message="httpclient is healthy")
    except Exception as e:
        logger.error("httpclient", message=str(e))

    with tracer.start_as_current_span("index") as span:
        span.set_attribute("key", "value")
        span.set_attribute("key2", "value2")
        span.set_attribute("key3", "value3")
        return Response(content=".", status_code=200)


@router.get("/{id}")
async def get(request: Request, id: int):
    tracer: trace.Tracer = request.app.state.tracer
    with tracer.start_as_current_span("get") as span:
        span.add_event("fetch to self")
        try:
            async with HttpClient.instance().get(
                "http://localhost:8443/health/"
            ) as response:
                if response.status != 200:
                    logger.info("httpclient", message="httpclient is not healthy")
                else:
                    logger.info("httpclient", message="httpclient is healthy")
        except Exception as e:
            logger.error("httpclient", message=str(e))
            span.record_exception(e)

        span.add_event("fetch rest api to location service")
        try:
            async with HttpClient.instance().get(
                "http://localhost:8444/location/1"
            ) as response:
                if response.status != 200:
                    logger.info("httpclient", message="httpclient is not healthy")
                else:
                    logger.info("httpclient", message="httpclient is healthy")
        except Exception as e:
            logger.error("httpclient", message=str(e))
            span.record_exception(e)

        span.add_event("fetch rest api to product service")
        try:
            async with HttpClient.instance().get(
                "http://localhost:8080/api/v1/products"
            ) as response:
                if response.status != 200:
                    logger.info("httpclient", message="httpclient is not healthy")
                else:
                    logger.info("httpclient", message="httpclient is healthy")
        except Exception as e:
            logger.error("httpclient", message=str(e))
            span.record_exception(e)

        span.add_event("query database")
        user = await user_repository.find(db=request.app.state.dbr, id=id)

        return JSONResponse(content=jsonable_encoder(user), status_code=200)


@router.post("/")
async def store(request: Request):
    tracer: trace.Tracer = request.app.state.tracer
    with tracer.start_span("store") as span:

        user = User(name="Purwandi", email="macman@gmail.com", address="Tangerang Banten")  # type: ignore

        try:
            await user_repository.create(db=request.app.state.dbw, user=user)
            return Response(content="", status_code=200)
        except Exception as e:
            print(e)
            span.record_exception(e)
            return Response(content="internal error", status_code=500)


@router.put("/{id}")
async def update(request: Request, id: int):
    tracer: trace.Tracer = request.app.state.tracer
    with tracer.start_span("update"):
        return Response(content=".", status_code=200)


@router.delete("/{id}")
async def destroy(request: Request, id: int):
    tracer: trace.Tracer = request.app.state.tracer
    with tracer.start_span("destroy"):
        return Response(content=".", status_code=200)
