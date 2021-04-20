package google

import (
	"net/http"

	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/herval/cloudsearch-desktop-agent/pkg/search/google"
)

func FetchableBuilder(account cloudsearch.AccountData) (fetchFns []cloudsearch.Fetchable, ids []string, err error) {
    return FetchablesFor(google.NewHttpClient(account), account)
}

func FetchablesFor(httpClient *http.Client, account cloudsearch.AccountData) ([]cloudsearch.Fetchable, []string, error) {
    drive, err := google.NewGoogleDrive(account, httpClient)
    if err != nil {
        return nil, nil, err
    }

    contacts, err := NewContacts(account, httpClient)
    if err != nil {
        return nil, nil, err
    }

    cals, err := NewCalendar(account, httpClient)
    if err != nil {
        return nil, nil, err
    }

    gmail, err := google.NewGmail(account, httpClient)
    if err != nil {
        return nil, nil, err
    }

    return []cloudsearch.Fetchable{
			cloudsearch.NewAsyncFetchable(contacts.FetchNewerThan),
			cloudsearch.NewAsyncFetchable(GdriveFetchNewerThan(drive)),
			cloudsearch.NewAsyncFetchable(cals.FetchNewerThan),
			cloudsearch.NewAsyncFetchable(GmailFetchNewerThan(gmail)),
    }, []string{
        "contacts",
        "drive",
        "calendar",
        "gmail",
    }, nil
}
