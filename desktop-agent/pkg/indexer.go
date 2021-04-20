package cloudsearch

import (
	"context"
	"fmt"
	"github.com/asdine/storm"
	"github.com/sirupsen/logrus"
	"sync"
	"time"
)

type Indexer interface {
	Start(ctx context.Context, wipeOutIndex bool) error
}

func NewResultIndexer(
	accounts AccountsStorage,
	results ResultsStorage,
	favorites FavoritesStorage,
	meta MetadataStorage,
	builder FetchableBuilder,
) Indexer {
	return &timeBoundIndexer{
		accounts:  accounts,
		results:   results,
		meta:      meta,
		builder:   builder,
		favorites: favorites,
	}
}

type timeBoundIndexer struct {
	accounts  AccountsStorage
	results   ResultsStorage
	meta      MetadataStorage
	favorites FavoritesStorage
	builder   FetchableBuilder
}

func (t *timeBoundIndexer) Start(ctx context.Context, wipeOutIndex bool) error {
	if wipeOutIndex {
		logrus.Info("*** Resetting local cache ***")
		err := t.wipeOut()
		if err != nil {
			return err
		}
	}

	for {
		if ctx.Err() != nil {
			return ctx.Err()
		}

		err := t.run(ctx)
		if err != nil {
			logrus.Error("Error running indexer ", err)
		}
		time.Sleep(time.Minute)
	}
}

func (t *timeBoundIndexer) run(ctx context.Context) error {
	acc, err := t.accounts.Active()
	if err != nil {
		return err
	}

	var wg sync.WaitGroup
	for _, a := range acc {
		wg.Add(1)
		go t.fetchAndUpdate(ctx, a, &wg) // TODO wait for these to finish
	}

	wg.Wait()
	t.prune()

	return nil
}

func (t *timeBoundIndexer) fetchAndUpdate(ctx context.Context, a AccountData, wg *sync.WaitGroup) {
	defer wg.Done()

	s, ids, err := t.builder(a)
	if err != nil {
		logrus.Error("could not fetch fetchables: ", err)
	}

	logrus.Debug("Fetching in bkg for account " + a.ID)
	for i, ss := range s {
		w, err := t.meta.WatermarkFor(a.ID, ids[i])
		if err != nil && err != storm.ErrNotFound {
			logrus.Error("could not fetch watermark: ", err)
		}
		//logrus.Debug("Current watermark: ", w)

		res, wm := ss(ctx, w)

		j := 0
		for r := range res {
			j += 1
			_, err := t.results.Merge(r)
			if err != nil {
				logrus.Error("could not save: ", err)
			}
		}

		m, ok := <-wm
		if !ok {
			logrus.Debug("No watermark was sent")
		}

		logrus.Debug(fmt.Sprintf("Fetched %d %s between %s - %s", j, ids[i], w, m))

		err = t.meta.SetWatermarkFor(a.ID, ids[i], m)
		if err != nil {
			logrus.Error("saving watermark", err)
		}

		//logrus.Debug("past for")

		//select {
		//case m := <-wm:
		//	//logrus.Debug("sync token for ", a.ID, " - "+m)
		//	err = t.meta.SetWatermarkFor(a.ID, ss.Name(), m)
		//	if err != nil {
		//		//logrus.Error("saving watermark", err)
		//	}
		//default:
		//	logrus.Debug("No watermark was sent")
		//}
	}
}

func (t *timeBoundIndexer) prune() {
	logrus.Debug("Pruning old content")

	// TODO trigger a full refresh in some case?

	res, err := t.results.FindOlderThan(time.Now().Add(-30 * 24 * time.Hour))
	if err != nil {
		logrus.Error("Deleting old content: ", err)
		return
	}

	for r := range res {
		// keep favorited
		if r.Favorited {
			logrus.Debug("Skipping pruning favorited ", r)
			continue
		}

		// contacts are expected to be "timeless", don't delete those
		if r.ContentType == Contact {
			logrus.Debug("Skipping contact", r)
			continue
		}

		err = t.results.Delete(r.Id)
		if err != nil {
			logrus.Error("cannot delete ", err)
		}
	}
}

func (t *timeBoundIndexer) wipeOut() error {
	res, err := t.results.FindOlderThan(time.Now())
	if err != nil {
		return err
	}

	i := 0
	for e := range res {
		if !e.Favorited {
			err = t.results.Delete(e.Id)
			i += 1
			if err != nil {
				logrus.Error("cannot delete ", err)
			}
		}
	}

	logrus.Debug("Deleted ", i, " results")

	return t.meta.ClearWatermarks()
}
