package action

import (
	"context"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/gocui"
	"github.com/sirupsen/logrus"
	"os"
)

func SearchAll(cmd string, search *cloudsearch.SearchEngine, r *cloudsearch.Registry) {
	query := cloudsearch.ParseQuery(cmd, cloudsearch.NewId(), r)
	res := search.Search(query, context.Background())

	for {
		select {
		case q, ok := <-res:
			if !ok {
				logrus.Debug("All done!")
				os.Exit(0)
			}
			logrus.Info("RESULT ->", q.Title)
		}
	}
}

func InteractiveMode(config cloudsearch.Config) error {
	return gocui.StartSearchApp(config.SearchEngine, config.Registry)
}
