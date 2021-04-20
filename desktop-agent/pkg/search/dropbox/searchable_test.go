package dropbox_test

import (
	"context"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/dropbox"
	"github.com/herval/cloudsearch-desktop-agent/pkg/test"
	"os"
	"testing"
	"time"

	"github.com/herval/cloudsearch-desktop-agent/pkg"
)

func TestDropbox(t *testing.T) {
	if os.Getenv("DROPBOX_TOKEN") == "" {
		t.Log("Skipping dropbox test (no token set)")
		t.Skipped()
	}

	d := dropbox.NewSearch(
		cloudsearch.AccountData{
			ID:    "123",
			Token: os.Getenv("DROPBOX_TOKEN"),
		},
	)

	var fetched *cloudsearch.Result = nil

	go func() {
		time.Sleep(time.Second * 10)
		if fetched == nil {
			t.Fatal("No data found")
		}
	}()

	data := d(cloudsearch.ParseQuery("clear_a.gif", "id", test.DefaultRegistry()), context.Background())

	fmt.Println(<-data)
}
