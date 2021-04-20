package cloudsearch_test

import (
	cloudsearch "github.com/herval/cloudsearch-desktop-agent/pkg"
	"testing"
)

func TestJsonFormat(t *testing.T) {
	q := cloudsearch.ParseQuery("hello world", "", cloudsearch.NewRegistry())
	r := &cloudsearch.Result{
		Title: "hello fam world",
		Body:  "thisworld is on fire",
	}

	json := cloudsearch.ResultJson(r, q, cloudsearch.RegularSearch, cloudsearch.Env{})
	if json["title"] != "<b>hello</b> fam <b>world</b>" {
		t.Fatal("Highlighting failed")
	}
}
