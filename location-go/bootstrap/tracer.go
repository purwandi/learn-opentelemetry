package bootstrap

import (
	"log"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/jaeger"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	"go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.4.0"
)

const (
	oTelService     = "location.backend"
	oTelEnvironment = "production"
	oTelAppVersion  = 1
)

func InitTracer(host, port, collector string) *trace.TracerProvider {
	exporter, err := jaeger.New(jaeger.WithAgentEndpoint(jaeger.WithAgentHost(host), jaeger.WithAgentPort(port)))

	if err != nil {
		log.Fatal(err)
	}
	tp := trace.NewTracerProvider(
		trace.WithBatcher(exporter),
		trace.WithResource(resource.NewWithAttributes(
			semconv.SchemaURL,
			semconv.ServiceNameKey.String(oTelService),
			attribute.String("environment", oTelEnvironment),
			attribute.Int64("ID", oTelAppVersion),
		)),
	)
	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(propagation.TraceContext{}, propagation.Baggage{}))
	return tp
}
