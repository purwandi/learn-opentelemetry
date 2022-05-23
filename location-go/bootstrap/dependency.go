package bootstrap

import (
	"github.com/segmentio/kafka-go"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/trace"
)

var (
	KAFKA_ADDR  = []string{"localhost:9092", "localhost:9093"}
	KAFKA_TOPIC = "product"
)

type Dependency struct {
	telemetry  trace.Tracer
	publisher  *kafka.Writer
	subscriber *kafka.Reader
}

func NewDependency() *Dependency {
	return &Dependency{}
}

func (d *Dependency) GetTracer() trace.Tracer {
	if d.telemetry != nil {
		return d.telemetry
	}

	d.telemetry = otel.Tracer(oTelService)
	return d.telemetry
}

func (d *Dependency) KafkaPublisher() *kafka.Writer {
	if d.publisher != nil {
		return d.publisher
	}

	w := kafka.NewWriter(kafka.WriterConfig{
		Brokers:  KAFKA_ADDR,
		Balancer: &kafka.LeastBytes{},
	})

	d.publisher = w
	return w
}

func (d *Dependency) KafkaSubscriber(topic, group string) *kafka.Reader {
	if d.subscriber != nil {
		return d.subscriber
	}

	r := kafka.NewReader(
		kafka.ReaderConfig{
			Brokers:  KAFKA_ADDR,
			Topic:    topic,
			GroupID:  group,
			MinBytes: 10e3, // 10KB
			MaxBytes: 10e6, // 10MB
		},
	)

	d.subscriber = r
	return r
}

func (d *Dependency) Close() {
	if d.publisher != nil {
		d.publisher.Close()
	}

	if d.subscriber != nil {
		d.subscriber.Close()
	}
}
