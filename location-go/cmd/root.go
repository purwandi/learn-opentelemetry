package cmd

import (
	"github.com/purwandi/location/bootstrap"
	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{}

func Execute() error {
	dependency := bootstrap.NewDependency()

	rootCmd.AddCommand(
		restCmd(dependency),
		workerCmd(dependency),
	)

	return rootCmd.Execute()
}
