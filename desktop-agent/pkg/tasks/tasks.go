package tasks

import (
	"context"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
)

type Task struct {

}

type TaskFetcher func(ctx context.Context, watermark string) (<-chan cloudsearch.Result, <-chan string)