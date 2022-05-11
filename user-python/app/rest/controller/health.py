from fastapi import APIRouter, Response, Request
from opentelemetry import trace


router = APIRouter()


@router.get("/")
async def index(request: Request):
  tracer: trace.Tracer = request.app.state.tracer
  with tracer.start_as_current_span("index"):
    return Response(content=".", status_code=200)
