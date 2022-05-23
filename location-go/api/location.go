package api

import (
	"fmt"
	"net/http"
	"time"

	"github.com/labstack/echo/v4"
	"github.com/purwandi/location/bootstrap"
	"github.com/purwandi/location/messages"
	"github.com/segmentio/kafka-go"
	"github.com/shopspring/decimal"
	"go.opentelemetry.io/otel/codes"
)

type Location struct {
	di *bootstrap.Dependency
}

func NewLocation(e *echo.Echo, di *bootstrap.Dependency) {
	server := &Location{
		di: di,
	}

	r := e.Group("/location")
	r.GET("", server.GetLocation)
	r.POST("", server.PostLocation)
}

func (server *Location) GetLocation(c echo.Context) error {
	_, span := server.di.GetTracer().Start(c.Request().Context(), "Kafka Publish")
	defer span.End()

	span.AddEvent("publishing to kafka")

	// compose message
	m := &messages.Product{
		ID:    1,
		Name:  "Product 1",
		Price: decimal.NewFromFloat(100.0),
		Telemetry: &messages.Telemetry{
			TraceId: span.SpanContext().TraceID().String(),
		},
	}
	messaging := kafka.Message{
		Topic: "product",
		Key:   []byte(fmt.Sprintf("%d", time.Now().Unix())),
		Value: m.ToBytes(),
	}

	pub := server.di.KafkaPublisher()
	if err := pub.WriteMessages(c.Request().Context(), messaging); err != nil {

		span.RecordError(err)
		span.SetStatus(codes.Error, err.Error())

		return c.JSON(http.StatusInternalServerError, map[string]interface{}{
			"error": err.Error(),
		})
	}
	span.AddEvent("published to kafka")

	return c.JSON(http.StatusOK, map[string]interface{}{
		"status": "OK",
	})
}

func (server *Location) PostLocation(c echo.Context) error {
	return c.JSON(http.StatusOK, map[string]interface{}{
		"status": "OK",
	})
}
