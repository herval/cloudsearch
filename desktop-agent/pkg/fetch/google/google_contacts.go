package google

import (
	"context"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/pkg/errors"
	"google.golang.org/api/people/v1"
	"net/http"
	"strings"
	"time"
)

type GoogleContacts struct {
	api          *people.Service
	googleClient *http.Client
	account      cloudsearch.AccountData
}

func NewContacts(
	account cloudsearch.AccountData,
	client *http.Client,
) (*GoogleContacts, error) {

	api, err := people.New(client)

	return &GoogleContacts{
		account: account,
		api:     api,
	}, err
}

func (a *GoogleContacts) FetchNewerThan(ctx context.Context, watermark string) ([]cloudsearch.Result, string, error) {
	// TODO delete stuff older than this?
	var err error
	var res []cloudsearch.Result
	var p []*people.Person
	pageToken := ""
	nextWatermark := ""

	for {
		p, nextWatermark, pageToken, err = a.contacts(
			ctx,
			watermark,
			pageToken,
		)
		if err != nil {
			return nil, "", err
		}

		for _, n := range p {
			//logrus.Debug(a.toResult(n))
			res = append(res, a.toResult(n))
		}

		if pageToken == "" || ctx.Err() != nil { // all done
			//logrus.Debug("Breaking")
			break
		}
	}

	return res, nextWatermark, ctx.Err()
}

func (a *GoogleContacts) contacts(ctx context.Context, syncToken string, pageToken string) ([]*people.Person, string, string, error) {
	//logrus.Debug("contact groups ", pageToken, " ", syncToken)
	pp, err := a.api.People.
		Connections.
		List("people/me").
		Context(ctx).
		SyncToken(syncToken).
		PageToken(pageToken).
		PageSize(100).
		RequestSyncToken(true).
		PersonFields("addresses,birthdays,emailAddresses,names,nicknames,organizations,photos,occupations,phoneNumbers,photos,metadata").
		Do()

	if err != nil {
		return nil, "", "", errors.Wrap(err, "fetching contacts")
	}

	return pp.Connections, pp.NextSyncToken, pp.NextPageToken, nil
}

func (a *GoogleContacts) toResult(f *people.Person) cloudsearch.Result {
	//logrus.Debug(fmt.Sprintf("%+v", f))

	status := cloudsearch.ResultFound
	if f.Metadata != nil && f.Metadata.Deleted {
		status = cloudsearch.ResultNotFound
	}

	n := name(f)
	p := photo(f)
	b := birthday(f)
	e := emails(f)
	ph := phones(f)
	j := jobs(f)
	aa := addresses(f)
	u := url(f)
	body := fmt.Sprintf("%s %s %s %s",
		strings.Join(emails(f), " "),
		strings.Join(phones(f), " "),
		strings.Join(addresses(f), " "),
		strings.Join(jobs(f), " "),
	)

	//logrus.Debug(body, n, p, b, e, ph, j, aa, u)

	return cloudsearch.Result{
		AccountId:   a.account.ID,
		AccountType: a.account.AccountType,
		Title:       n,
		Permalink:   u,
		Thumbnail:   p,
		Timestamp:   time.Time{},
		OriginalId:  f.ResourceName,
		ContentType: cloudsearch.Contact,
		Details: map[string]interface{}{
			"name":      n,
			"emails":    e,
			"phones":    ph,
			"jobs":      j,
			"addresses": aa,
			"birthday":  b,
			"photo":     p,
		},
		Body:   body,
		Status: status,
	}
}

func birthday(person *people.Person) string {
	if len(person.Birthdays) > 0 {
		return person.Birthdays[0].Text
	}
	return ""
}

func addresses(person *people.Person) []string {
	var res []string
	for _, a := range person.Addresses {
		res = append(res, a.FormattedValue)
	}
	return res
}

func phones(person *people.Person) []string {
	var res []string
	for _, a := range person.PhoneNumbers {
		res = append(res, a.Value)
	}
	return res
}

func emails(person *people.Person) []string {
	var res []string
	for _, a := range person.EmailAddresses {
		res = append(res, a.Value)
	}
	return res
}

func jobs(person *people.Person) []string {
	var res []string
	for _, a := range person.Organizations {
		res = append(res, fmt.Sprintf("%s %s", a.Title, a.Name))
	}
	return res
}

func photo(person *people.Person) string {
	if len(person.Photos) == 0 {
		return ""
	}
	return person.Photos[0].Url
}

func url(person *people.Person) string {
	if len(person.Urls) == 0 {
		return ""
	}
	return person.Urls[0].Value
}

func name(person *people.Person) string {
	if len(person.Names) == 0 {
		if len(person.Nicknames) == 0 {
			return ""
		}
		return person.Nicknames[0].Value
	}
	return person.Names[0].DisplayName
}
