package google

import (
	"context"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/google"
	"github.com/pkg/errors"
	"time"
)

func GmailFetchNewerThan(a *google.Gmail) func(ctx context.Context, watermark string) ([]cloudsearch.Result, string, error) {
	return func(ctx context.Context, watermark string) ([]cloudsearch.Result, string, error) {
		t0 := SyncCutoff(MaxDaysToSync, watermark, google.GmailTimeFormat)

		watermark = a.FormattedTime(t0)

		// TODO delete stuff older than this?

		nextSyncToken := a.FormattedTime(time.Now())

		pageToken := ""
		var err error
		var res []cloudsearch.Result

		q := ""
		if watermark != "" {
			q = fmt.Sprintf("after:%s", watermark)
		}

		for {
			r := make(chan cloudsearch.Result)
			pageToken, err = a.Search(
				ctx,
				q,
				pageToken,
				r,
			)

			if err != nil {
				return res, watermark, errors.Wrap(err, "fetching gmail results")
			}

			for f := range r {
				res = append(res, f)
			}

			if pageToken == "" || ctx.Err() != nil { // all done
				//logrus.Debug("done!")
				return res, nextSyncToken, ctx.Err()
			}
		}
	}
}