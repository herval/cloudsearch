package google

import (
	"context"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/pkg/errors"
	"google.golang.org/api/calendar/v3"
	"net/http"
	"time"
)

type GoogleCalendarEvents struct {
	api          *calendar.Service
	googleClient *http.Client
	account      cloudsearch.AccountData
}

func NewCalendar(
	account cloudsearch.AccountData,
	client *http.Client,
) (*GoogleCalendarEvents, error) {

	api, err := calendar.New(client)

	return &GoogleCalendarEvents{
		account: account,
		api:     api,
	}, err
}

func (a *GoogleCalendarEvents) FetchNewerThan(ctx context.Context, watermark string) ([]cloudsearch.Result, string, error) {
	// TODO delete stuff older than this?
	var err error
	var res []cloudsearch.Result
	var p []*calendar.Event

	p, watermark, err = a.events(
		ctx,
		watermark,
	)
	if err != nil {
		return nil, watermark, err
	}

	for _, n := range p {
		e := a.toResult(n)
		if e != nil {
			res = append(res, *e)
		}
	}

	//logrus.Debug(res)

	return res, watermark, ctx.Err()
}

func (a *GoogleCalendarEvents) events(ctx context.Context, syncToken string) ([]*calendar.Event, string, error) {
	//logrus.Debug("contact groups ", pageToken, " ", syncToken)
	nextSyncToken := time.Now().Format(time.RFC3339)
	cal, err := a.api.CalendarList.List().Do()
	if err != nil {
		return nil, "", errors.Wrap(err, "fetching events")
	}

	t0 := SyncCutoff(MaxDaysToSync, syncToken, time.RFC3339)

	var evts []*calendar.Event
	for _, c := range cal.Items {
		// TODO parallelize this
		e, err := a.fetchEvents(ctx, c.Id, syncToken, t0)
		if err != nil {
			return nil, "", err
		}
		evts = append(evts, e...)
	}

	return evts, nextSyncToken, nil
}

func (a *GoogleCalendarEvents) fetchEvents(ctx context.Context, calId string, syncToken string, timeMin time.Time) ([]*calendar.Event, error) {
	// this doesnt do a full sync - if you add/remove a cal item in the past, it'll not be updated on a second pass
	pageToken := ""
	var evts []*calendar.Event

	for {
		pp, err := a.api.Events.
			List(calId).
			TimeMin(timeMin.Format(time.RFC3339)).
			Context(ctx).
			PageToken(pageToken).
			Do()

		if err != nil {
			return nil, errors.Wrap(err, "fetching cal events")
		}

		pageToken = pp.NextPageToken
		//nextSync = pp.NextSyncToken
		evts = append(evts, pp.Items...)

		if pageToken == "" {
			break
		}
	}

	return evts, nil
}

func (a *GoogleCalendarEvents) toResult(f *calendar.Event) *cloudsearch.Result {
	// stuff we dont care about
	if f.Start == nil || f.Status == "cancelled" {
		//logrus.Debug("Skipping: ", f)
		return nil
	}

	organizer := ""
	if f.Organizer != nil {
		organizer = f.Organizer.DisplayName
	}
	var start string
	if f.Start != nil {
		start = f.Start.DateTime
	}

	var end string
	if f.End != nil {
		start = f.End.DateTime
	}

	var ts time.Time
	var err error
	if start != "" {
		ts, err = time.Parse(time.RFC3339, start)
	} else if f.Updated != "" {
		ts, err = time.Parse(time.RFC3339, f.Updated)
		if err != nil {
			ts, _ = time.Parse(time.RFC3339, f.Created)
		}
	}

	return &cloudsearch.Result{
		AccountId:   a.account.ID,
		AccountType: a.account.AccountType,
		Title:       f.Summary,
		Permalink:   f.HtmlLink,
		Thumbnail:   "",
		Timestamp:   ts,
		OriginalId:  f.ICalUID,
		ContentType: cloudsearch.Event,
		Details: map[string]interface{}{
			"status":    f.Status,
			"dial_in":   f.HangoutLink,
			"organizer": organizer,
			"start_at":  start,
			"end_at":    end,
			"location":  f.Location,
		},
		Body: fmt.Sprintf("%s %s", f.Summary, f.Location),
	}
}
