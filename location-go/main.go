package main

import (
	"fmt"

	"github.com/purwandi/location/cmd"
	"github.com/spf13/viper"
	"go.opentelemetry.io/otel"
)

var tracer = otel.Tracer("location.backend")

func main() {
	viper.SetConfigName("config.yaml")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")    // optionally look for config in the working directory
	err := viper.ReadInConfig() // Find and read the config file
	if err != nil {             // Handle errors reading the config file
		panic(fmt.Errorf("fatal error config file: %w \n", err))
	}

	if err := cmd.Execute(); err != nil {
		panic(err)
	}
}
