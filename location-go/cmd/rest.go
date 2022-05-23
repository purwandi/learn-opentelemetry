package cmd

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
	"github.com/purwandi/location/api"
	"github.com/purwandi/location/bootstrap"
	"github.com/purwandi/location/midd"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"go.opentelemetry.io/contrib/instrumentation/github.com/labstack/echo/otelecho"
)

func restCmd(d *bootstrap.Dependency) *cobra.Command {
	return &cobra.Command{
		Use: "rest",
		Run: func(cmd *cobra.Command, args []string) {

			agent := strings.Split(viper.GetString("tracer.agent"), ":")
			tp := bootstrap.InitTracer(agent[0], agent[1], viper.GetString("tracer.collector"))

			e := echo.New()
			e.Use(otelecho.Middleware(viper.GetString("tracer.name")))
			e.Use(middleware.Recover())
			e.Use(midd.Logger())

			api.NewLocation(e, d)

			// e.GET("/location/:id", func(c echo.Context) error {
			// 	id := c.Param("id")
			// 	// spctx, span := tracer.Start(c.Request().Context(), "get user", oteltrace.WithAttributes(attribute.String("id", id)))
			// 	// defer span.End()

			// 	time.Sleep(2 * time.Millisecond)

			// 	_, span2 := d.GetTracer().Start(c.Request().Context(), "get database")
			// 	defer span2.End()
			// 	time.Sleep(10 * time.Millisecond)

			// 	_, span3 := d.GetTracer().Start(c.Request().Context(), "get redis")
			// 	defer span3.End()
			// 	time.Sleep(1 * time.Millisecond)

			// 	return c.HTML(http.StatusOK, id)
			// })

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

			// closing tracer
			if err := tp.Shutdown(context.Background()); err != nil {
				log.Printf("Error shutting down tracer provider: %v", err)
			}

			// closing kafka publisher
			d.Close()

			if err := e.Shutdown(ctx); err != nil {
				e.Logger.Fatal(err)
			}
		},
	}

}
