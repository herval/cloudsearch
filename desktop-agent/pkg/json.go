package cloudsearch

import (
	"fmt"
	"github.com/sirupsen/logrus"
	"regexp"
	"strings"
)

func ResultJson(r *Result, q Query, searchType SearchType, env Env) map[string]interface{} {
	return map[string]interface{}{
		"id":              r.Id,
		"accountId":       r.AccountId,
		"sourceType":      r.AccountType,
		"sourceIcon":      AccountIcon(r.AccountType),
		"title":           formatAndHighlight(r.Title, q),
		"snippet":         formatAndHighlight(r.Body, q),
		"permalink":       r.Permalink,
		"thumbnail":       localThumbnail(r, env),
		"timestamp":       r.Timestamp,
		"timestampMillis": r.Timestamp.Unix() * 1000,
		"order":           r.Relevance(q),
		"favorited":       r.Favorited,
		"previewable":     previewableResult(r),
		"kind":            r.ContentType,
		"details":         r.Details,
		"cached":          !r.CachedAt.IsZero(),
		"icon":            ContentIcon(r, q),
		"searchType":      string(searchType),
	}
}

func localThumbnail(r *Result, env Env) string {
	if r.Thumbnail == "" {
		return ""
	}

	return fmt.Sprintf("http://localhost%s/contents/thumbnail/%s/%s", env.HttpPort, r.AccountId, r.Id)
}

func previewableResult(result *Result) bool {
	return result.ContentType != SearchQuery
}

func formatAndHighlight(s string, q Query) string {
	if len(q.Text) == 0 {
		return s
	}

	terms := strings.Split(q.Text, " ")
	for _, t := range terms {
		r, err := regexp.Compile(`(?i)` + t)
		if err != nil {
			logrus.Error("Could not compile: ", t)
		} else {
			s = r.ReplaceAllString(s, fmt.Sprintf("<b>%s</b>", t))
		}
	}
	return s
}
