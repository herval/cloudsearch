package main

import (
	"context"
	"flag"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/action"
	"github.com/herval/cloudsearch-desktop-agent/pkg/api"
	"github.com/herval/cloudsearch-desktop-agent/pkg/config"
	"os"
	"strings"

	"github.com/sirupsen/logrus"
)

func main() {
	storagePath := flag.String("storagePath", "./", "storage path") // TODO . folder?
	httpPort := flag.String("httpPort", ":65432", "HTTP Port for Oauth2 callbacks and server mode")
	format := flag.String("format", "plain", "Output format for results (plain, json)")
	debug := flag.Bool("debug", false, "Debug logging")
	log := flag.Bool("log", false, "Output logging to a file")
	backgroundIndex := flag.Bool("index", false, "turn on background indexing")
	reindex := flag.Bool("reindex", false, "wipe out the cached items (keeping favorited)")
	flag.Parse()

	err := cloudsearch.ConfigureLogging(*debug, *log)
	if err != nil {
		panic(err)
	}

	env := cloudsearch.Env{
		ServerBase:  "http://localhost",
		StoragePath: *storagePath,
		HttpPort:    *httpPort,
	}

	config, err := config.NewConfig(env, true)
	if err != nil {
		panic(err)
	}

	mode := flag.Arg(0)
	switch mode {
	case "accounts":
		op := flag.Arg(1)
		acc := flag.Arg(2)
		action.ListOrRemove(config.AccountsStorage, op, acc, *format)
	case "login":
		accType := flag.Arg(1)
		action.ConfigureNewAccount(accType, config.Env, config.AccountsStorage, config.Registry, config.AuthService)
	case "search":
		searchString := strings.Join(flag.Args()[1:], " ")
		action.SearchAll(searchString, config.SearchEngine, config.Registry)
	case "index":
		startIndexer(config.ResultIndexer, *reindex)
	case "server":
		if *backgroundIndex || *reindex {
			go startIndexer(config.ResultIndexer, *reindex)
		}
		startServer(
			*debug,
			config,
		)
	case "interactive":
		err := action.InteractiveMode(config)
		if err != nil {
			fmt.Println(err.Error())
			os.Exit(1)
		}
	default:
		flag.Usage()
	}
}

func startIndexer(indexer cloudsearch.Indexer, wipeOutIndex bool) {
	logrus.Info("Starting indexer...")
	err := indexer.Start(context.Background(), wipeOutIndex)
	if err != nil {
		panic(err)
	}
}

// TODO use cloudsearch.Config
func startServer(
	debug bool,
	config cloudsearch.Config,
) {
	a := &api.Api{
		config,
		nil,
	}
	if err := a.Start(config.Env.HttpPort, debug); err != nil {
		logrus.Fatal("Could not start server", err)
	}
}
