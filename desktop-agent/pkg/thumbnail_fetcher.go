package cloudsearch

import (
	"github.com/sirupsen/logrus"
	"io/ioutil"
	"net/http"
)

type ThumbnailHandler struct {
	Meta   MetadataStorage
	Client *http.Client
}

func (f *ThumbnailHandler) FetchAndSave(result Result) error {
	if result.Thumbnail == "" {
		logrus.Debug("No thumbnail for ", result.Id)
		return nil
		//return errors.New("no thumbnail url")
	}

	logrus.Debug("Downloading ", result.Thumbnail)
	res, err := f.Client.Get(result.Thumbnail)
	if err != nil {
		return err
	}

	defer res.Body.Close()

	body, err := ioutil.ReadAll(res.Body)
	if err != nil {
		return err
	}

	return f.Meta.SaveThumbnail(result.Id, body)
}

func (f *ThumbnailHandler) GetThumbnail(result Result) ([]byte, string, error) {
	data, err := f.Meta.GetThumbnail(result.Id)
	if err != nil {
		return nil, "", err
	}

	kind := http.DetectContentType(data)
	return data, kind, err
}

func (f *ThumbnailHandler) Delete(resultId string) error {
	return f.Meta.DeleteThumbnail(resultId)
}

func FetchThumbsFilter(t ThumbnailHandler) func(query Query, in Result) *Result {
	return func(query Query, in Result) *Result {
		err := t.FetchAndSave(in) // TODO should this be in background??
		if err != nil {
			logrus.WithFields(logrus.Fields{
				"result": in.Id,
				"err":    err,
			}).Debug("Could not fetch thumbnail")
		}

		return &in
	}
}
