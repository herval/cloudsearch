package google

import (
	"context"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/google"
	"github.com/pkg/errors"
	"time"

	"google.golang.org/api/drive/v3"
)

func GdriveFetchNewerThan(a *google.GoogleDrive) func(ctx context.Context, watermark string) ([]cloudsearch.Result, string, error) {
	return func(ctx context.Context, watermark string) ([]cloudsearch.Result, string, error) {
		t0 := SyncCutoff(MaxDaysToSync, watermark, time.RFC3339)
		nextWatermark := a.FormattedTime(time.Now())

		tt := a.FormattedTime(t0)
		watermark = tt

		pageToken := ""
		var r *drive.FileList
		var err error
		var res []cloudsearch.Result

		for {
			r, pageToken, err = a.Search(
				ctx,
				fmt.Sprintf("modifiedTime > '%s' OR createdTime > '%s'", tt, tt),
				pageToken,
			)

			if err != nil {
				return nil, "", errors.Wrap(err, "fetching gdrive results")
			}

			for _, f := range r.Files {
				res = append(res, a.ToResult(f))
			}

			if r.NextPageToken == "" || ctx.Err() != nil { // all done
				//logrus.Debug("done!")
				return res, nextWatermark, ctx.Err()
			}
		}
	}
}
