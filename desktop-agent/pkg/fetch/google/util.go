package google

import "time"


var MaxDaysToSync = 30 * 6

func SyncCutoff(maxDaysAgo int, watermarkStr string, format string) time.Time {
	t0 := time.Now().Add(-time.Hour * (24 * time.Duration(maxDaysAgo)))
	if watermarkStr != "" {
		t, err := time.Parse(format, watermarkStr)
		if err == nil && t.After(t0) {
			return t
		}
	}

	return t0
}
