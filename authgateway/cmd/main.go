package main

import (
	"flag"
	"github.com/gin-gonic/gin"
	"github.com/herval/authgateway"
	"os"
)

func main() {
	httpPort := flag.String("httpPort", ":8080", "HTTP Port")
	config := flag.String("config", "", "Service config file")
	debug := flag.Bool("debug", false, "Run in debug mode")
	flag.Parse()

	if *config == "" {
		flag.PrintDefaults()
		os.Exit(1)
	}

	if !*debug {
		gin.SetMode(gin.ReleaseMode)
	}

	env, err := authgateway.ParseConfig(*config)
	if err != nil {
		panic(err)
	}

	api := authgateway.Api{}

	err = api.StartServer(*httpPort, *env)
	if err != nil {
		panic(err)
	}
}
