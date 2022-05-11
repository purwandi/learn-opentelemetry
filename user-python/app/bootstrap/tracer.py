from typing import AnyStr
from opentelemetry import trace
from opentelemetry.exporter.jaeger.thrift import JaegerExporter
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor

# from opentelemetry.instrumentation.asyncpg import AsyncPGInstrumentor


def tracer_create(name: str, agent: str, collector: str):
    if agent == "":
        agent = "localhost:6831"

    resource = Resource.create({SERVICE_NAME: name})
    trace.set_tracer_provider(TracerProvider(resource=resource))
    tracer = trace.get_tracer(__name__)

    hostname, port = agent.split(":")
    exporter = JaegerExporter(
        agent_host_name=hostname, agent_port=int(port), collector_endpoint=collector
    )
    trace.get_tracer_provider().add_span_processor(BatchSpanProcessor(exporter))  # type: ignore

    return tracer


# trace, tracer = tracer_create(v.get("tracer.name"), v.get("tracer.endpoint"))
