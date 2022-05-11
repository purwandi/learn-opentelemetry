from fastapi import Request, Response
from opentelemetry.propagate import extract
from opentelemetry import trace
from opentelemetry.trace.status import Status, StatusCode
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.types import ASGIApp
from starlette.routing import Match
from app.bootstrap.logger import logger

from datetime import datetime


def get_route_path(request: Request) -> str:
    route = ""
    for r in request.app.routes:
        if r.matches(request.scope)[0] == Match.FULL:
            route = r.path
            break

        if r.matches(request.scope)[0] == Match.PARTIAL:
            route = r.path

    return route


# https://stackoverflow.com/questions/71525132/how-to-write-a-custom-fastapi-middleware-class
class HttpTrace(BaseHTTPMiddleware):
    def __init__(self, app: ASGIApp) -> None:
        super().__init__(app)

    async def dispatch(self, request: Request, call_next) -> Response:

        with request.app.state.tracer.start_as_current_span(
            get_route_path(request) + " " + request.method,
            context=extract(request.headers),
            kind=trace.SpanKind.SERVER,
            # attributes=collect_request_attributes(request.environ),
        ) as span:
            span.set_attribute("http.method", request.method)
            span.set_attribute("http.url", request.url.path)
            start_time = datetime.now()

            response = await call_next(request)

            process_time = datetime.now() - start_time
            logger.bind(
                trace_id=format(span.get_span_context().trace_id, "032x"),
                span_id=format(span.get_span_context().span_id, "016x"),
                http_scheme=request.url.scheme,
                http_method=request.method,
                http_url=request.url.path,
                http_status=response.status_code,
                resp_elapsed=process_time.total_seconds(),
                resp_elapsed_ms=round(process_time.total_seconds() * 1000, 2),
                user_agent=request.headers.get("user-agent"),
            ).info("middleware")

            if response.status_code >= 400:
                span.set_attribute("http.status_code", response.status_code)
                span.set_status(Status(status_code=StatusCode.ERROR))

            return response
