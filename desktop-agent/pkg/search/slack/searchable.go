package slack

import (
	"context"
	"fmt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/nlopes/slack"
	"github.com/sirupsen/logrus"
	"strings"
)

func NewSearch(account cloudsearch.AccountData) cloudsearch.SearchFunc {
	client := slack.New(account.Token)
	types := append(cloudsearch.FileTypes, cloudsearch.Message)

	return func(query cloudsearch.Query, context context.Context) <-chan cloudsearch.Result {
		out := make(chan cloudsearch.Result)

		if !cloudsearch.CanHandle(query, account.AccountType, types) {
			defer close(out)
			return out
		}

		go func() {
			defer close(out)

			messages, files, err := client.Search(query.Text, slack.SearchParameters{})
			if err != nil {
				logrus.Trace("Error searching:", err)
				return
			}

			if len(query.ContentTypes) == 0 || cloudsearch.ContainsType(query.ContentTypes, cloudsearch.Message) {
				for _, m := range messages.Matches {
					out <- toMessageResult(account, m)
				}
			}

			if len(query.ContentTypes) == 0 || cloudsearch.ContainsAnyType(query.ContentTypes, cloudsearch.FileTypes) {
				for _, m := range files.Matches {
					out <- toFileResult(account, m)
				}
			}
		}()

		return out
	}

}

func messageText(msg slack.CtxMessage) string {
	if msg.Text != "" {
		return fmt.Sprintf("%s: %s", msg.User, msg.Text)
	}
	return ""
}

func toMessageResult(acc cloudsearch.AccountData, message slack.SearchMessage) cloudsearch.Result {
	mainMsg := fmt.Sprintf("%s: %s", message.Username, message.Text)
	msg := []string{}
	msg = append(msg, messageText(message.Previous2))
	msg = append(msg, messageText(message.Previous))
	msg = append(msg, mainMsg)
	msg = append(msg, messageText(message.Next))
	msg = append(msg, messageText(message.Next2))

	single := mainMsg

	return cloudsearch.Result{
		AccountId:   acc.ID,
		AccountType: acc.AccountType,
		Title:       single,
		Permalink:   message.Permalink,
		Thumbnail:   "",
		Timestamp:   cloudsearch.TimeFromFloatMillis(message.Timestamp),
		ContentType: cloudsearch.Message,
		Details: map[string]interface{}{
			"body": strings.Join(msg, "\n"), //display more context
		},
		OriginalId: message.Permalink,
		Body:       single, // context isn't searchable, only the message itself
		InvolvesMe: message.User == acc.ExternalId || strings.Contains(message.Text, acc.Name),
	}
}

func toFileResult(acc cloudsearch.AccountData, file slack.File) cloudsearch.Result {
	// TODO files
	return cloudsearch.Result{}
}
