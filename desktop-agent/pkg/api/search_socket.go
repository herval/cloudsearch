package api

import (
	"context"
	"encoding/json"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/sirupsen/logrus"
	"gopkg.in/olahol/melody.v1"
)

func (a *Api) searchSocket(m *melody.Session, b []byte) {
	op, err := parseMessage(b)
	if err != nil {
		_ = m.Write(
			jsonBytes(
				map[string]interface{}{
					"type":     "error",
					"error":    err.Error(),
					"searchId": op.SearchId,
				},
			),
		)
		return
	}

	//logrus.Debug(op)

	// TODO this doesnt actually cancel?
	if a.CurrentSearchCancel != nil {
		logrus.Debug("Cancelling inflight request...")
		a.CurrentSearchCancel()
	}

	switch op.Op {
	case "cancel":
		// nothing more to do here
	case "search":
		go doSearch(&op, m, a)
	}
}

func doSearch(op *socketMsg, m *melody.Session, a *Api) {
	total := 0

	if op.Query == "" {
		_ = m.Write(
			jsonBytes(
				map[string]interface{}{
					"type":     "error",
					"error":    "You need to specify a query",
					"searchId": op.SearchId,
				},
			),
		)
	}

	ctx, cancel := context.WithCancel(m.Request.Context())
	a.CurrentSearchCancel = cancel

	q := cloudsearch.ParseQuery(op.Query, op.SearchId, a.Config.Registry)
	for res := range a.Config.SearchEngine.Search(q, ctx) {
		if ctx.Err() != nil {
			break
		}

		if res.Status == cloudsearch.ResultFound {
			total += 1
			_ = m.Write(
				jsonBytes(
					map[string]interface{}{
						"type":     "result",
						"result":   cloudsearch.ResultJson(&res, q, cloudsearch.RegularSearch, a.Config.Env),
						"searchId": op.SearchId,
					},
				),
			)
		} else {
			logrus.Error("Unwanted result: ", q)
		}
	}

	_ = m.Write(
		jsonBytes(
			map[string]interface{}{
				"type":         "status",
				"searchId":     op.SearchId,
				"status":       "done",
				"totalResults": total,
			}))
}

func jsonBytes(data map[string]interface{}) []byte {
	by, _ := json.Marshal(data)
	return by
}

func parseMessage(b []byte) (socketMsg, error) {
	m := socketMsg{}
	err := json.Unmarshal(b, &m)
	if m.SearchId == "" {
		m.SearchId = cloudsearch.NewId()
	}
	return m, err
}

type socketMsg struct {
	Op       string `json:"op"`
	SearchId string `json:"searchId"`
	Query    string `json:"query"`
}
