package cloudsearch

import (
	"context"
	"github.com/sirupsen/logrus"
)

type FetchableBuilder func(account AccountData) (fetchFns []Fetchable, ids []string, err error)

type Fetchable func(ctx context.Context, watermark string) (<-chan Result, <-chan string)

type SyncFetchable func(ctx context.Context, watermark string) ([]Result, string, error)

// wrap a fetchable in a channel-bound thing
func NewAsyncFetchable(
	fetchable SyncFetchable,
) Fetchable {
	return func(ctx context.Context, watermark string) (<-chan Result, <-chan string) {
		out := make(chan Result)
		mark := make(chan string, 1)

		go func() {
			defer close(out)
			defer close(mark)

			res, watermark, err := fetchable(ctx, watermark)
			if res != nil {
				for _, r := range res {
					out <- r
				}
			}

			if watermark != "" {
				//logrus.Debug("mark: ", watermark)
				mark <- watermark
			}

			if err != nil {
				logrus.Error("Fetching failed ", err)
			}
		}()

		return out, mark
	}
}
