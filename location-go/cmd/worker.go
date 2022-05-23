package cmd

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/purwandi/location/bootstrap"
	"github.com/purwandi/location/messages"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/trace"
)

func workerCmd(di *bootstrap.Dependency) *cobra.Command {
	worker := &cobra.Command{
		Use: "worker",
		Run: func(cmd *cobra.Command, args []string) {
			topic, _ := cmd.Flags().GetString("topic")
			group, _ := cmd.Flags().GetString("group")

			agent := strings.Split(viper.GetString("tracer.agent"), ":")
			tp := bootstrap.InitTracer(agent[0], agent[1], viper.GetString("tracer.collector"))

			fmt.Println("start consuming ... :", topic)
			reader := di.KafkaSubscriber(topic, group)

			defer func() {
				reader.Close()

				// closing tracer
				if err := tp.Shutdown(context.Background()); err != nil {
					log.Printf("Error shutting down tracer provider: %v", err)
				}

				// closing kafka publisher
				di.Close()

				fmt.Println("shutdown ... !!")
			}()

			for {
				m, err := reader.ReadMessage(context.Background())
				if err != nil {
					log.Fatal(err)
				}

				fmt.Println("hello")

				var product messages.Product
				json.Unmarshal(m.Value, &product)
				Process(di, group, product)

			}
		},
	}

	worker.PersistentFlags().String("topic", "", "subscribe to kafka topic")
	worker.PersistentFlags().String("group", "worker-main", "subscribe to kafka topic as group")
	return worker
}

func Process(di *bootstrap.Dependency, group string, m messages.Product) {
	_, span := di.GetTracer().Start(context.Background(), group, trace.WithAttributes(
		attribute.String("component", group),
		attribute.String("trace_id", m.Telemetry.TraceId),
	))
	defer span.End()

	fmt.Println(fmt.Sprintf("%+v", m))

	span.AddEvent("start processing")
	fmt.Println("start processing")
	time.Sleep(time.Second * 5)
	span.AddEvent("done processing")
	fmt.Println("done processing")
}
