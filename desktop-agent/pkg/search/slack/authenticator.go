package slack

import (
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/nlopes/slack"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
)

func NewAuthenticator() cloudsearch.IdentityService {
	return &SlackAuth{
	}
}

type SlackAuth struct {
}

func (d *SlackAuth) RefreshAccountIfNeeded(a cloudsearch.AccountData) (acc cloudsearch.AccountData, accountChanged bool, err error) {
	return a, false, nil
}

func (d *SlackAuth) FetchIdentityInfo(data cloudsearch.AccountData) (*cloudsearch.AccountData, error) {
	logrus.Debug("Fetching identity...")
	c := slack.New(data.Token)
	dd, err := c.AuthTest()
	if err != nil {
		return nil, errors.Wrap(err, "fetching User info")
	}

	data.Name = dd.User
	data.ExternalId = dd.UserID
	data.Email = dd.UserID + " @ " + dd.TeamID
	data.Description = dd.UserID + " @ " + dd.TeamID

	return &data, nil
}
