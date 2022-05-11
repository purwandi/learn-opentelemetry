package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"time"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/purwandi/location/midd"
	"github.com/spf13/viper"
	"go.opentelemetry.io/contrib/instrumentation/github.com/labstack/echo/otelecho"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/jaeger"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	"go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.10.0"
)

const (
	service     = "location.backend"
	environment = "production"
	id          = 1
)

var tracer = otel.Tracer("location.backend")

func initTracer(host, port, collector string) *trace.TracerProvider {
	// exporter, err := jaeger.New(jaeger.WithCollectorEndpoint(jaeger.WithEndpoint(collector)))
	exporter, err := jaeger.New(jaeger.WithAgentEndpoint(jaeger.WithAgentHost(host), jaeger.WithAgentPort(port)))

	if err != nil {
		log.Fatal(err)
	}
	tp := trace.NewTracerProvider(
		trace.WithBatcher(exporter),
		trace.WithResource(resource.NewWithAttributes(
			semconv.SchemaURL,
			semconv.ServiceNameKey.String(service),
			attribute.String("environment", environment),
			attribute.Int64("ID", id),
		)),
	)
	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(propagation.TraceContext{}, propagation.Baggage{}))
	return tp
}

func main() {
	viper.SetConfigName("config.yaml")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")    // optionally look for config in the working directory
	err := viper.ReadInConfig() // Find and read the config file
	if err != nil {             // Handle errors reading the config file
		panic(fmt.Errorf("fatal error config file: %w \n", err))
	}

	agent := strings.Split(viper.GetString("tracer.agent"), ":")
	tp := initTracer(
		agent[0],
		agent[1],
		viper.GetString("tracer.collector"),
	)

	e := echo.New()
	e.Use(otelecho.Middleware(viper.GetString("tracer.name")))
	e.Use(middleware.Recover())
	e.Use(midd.Logger())

	e.GET("/location/:id", func(c echo.Context) error {
		id := c.Param("id")
		// spctx, span := tracer.Start(c.Request().Context(), "get user", oteltrace.WithAttributes(attribute.String("id", id)))
		// defer span.End()

		time.Sleep(2 * time.Millisecond)

		_, span2 := tracer.Start(c.Request().Context(), "get database")
		defer span2.End()
		time.Sleep(1 * time.Millisecond)

		_, span3 := tracer.Start(c.Request().Context(), "get redis")
		defer span3.End()
		time.Sleep(1 * time.Millisecond)

		return c.HTML(http.StatusOK, id)
	})

	// Start server
	go func() {
		if err := e.Start(fmt.Sprintf(":%d", viper.GetInt("port"))); err != nil && err != http.ErrServerClosed {
			e.Logger.Fatal("shutting down the server", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, os.Interrupt)
	<-quit
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := tp.Shutdown(context.Background()); err != nil {
		log.Printf("Error shutting down tracer provider: %v", err)
	}
	if err := e.Shutdown(ctx); err != nil {
		e.Logger.Fatal(err)
	}
}
