package storm

import (
	"fmt"
	"github.com/asdine/storm"
	"github.com/boltdb/bolt"
	"github.com/herval/cloudsearch-desktop-agent/pkg"
)

type MetadataStorage struct {
	s *storm.DB
}

func NewMetadataStorage(storagePath string) (cloudsearch.MetadataStorage, error) {
	db, err := storm.Open(cloudsearch.FileAt(storagePath, "meta.db"))
	if err != nil {
		return nil, err
	}

	return &MetadataStorage{
		s: db,
	}, nil
}

func (m *MetadataStorage) SaveThumbnail(searchableId string, data []byte) error {
	return m.s.SetBytes(
		"thumbs",
		cloudsearch.Md5(searchableId),
		data,
	)
}

func (m *MetadataStorage) GetThumbnail(searchableId string) ([]byte, error) {
	b, err := m.s.GetBytes("thumbs", cloudsearch.Md5(searchableId))
	return b, err
}

func (m *MetadataStorage) DeleteThumbnail(searchableId string) error {
	return m.s.Delete("thumbs", cloudsearch.Md5(searchableId))
}

func (m *MetadataStorage) ClearWatermarks() error {
	err := m.s.Drop("watermark")
	if err != nil && err.Error() != bolt.ErrBucketNotFound.Error() { // ugh this sucks
		return err
	}

	tx, err := m.s.Bolt.Begin(true)
	_, err = m.s.CreateBucketIfNotExists(tx, "watermark")
	return err
}

func (m *MetadataStorage) SetWatermarkFor(accountId string, searchableId string, watermark string) error {
	//logrus.Debug("Setting watermark for ", accountId, " ", searchableId, " to ", watermark)
	return m.s.SetBytes("watermark", m.keyFor(accountId, searchableId), []byte(watermark))
}

func (m *MetadataStorage) WatermarkFor(accountId string, searchableId string) (string, error) {
	b, err := m.s.GetBytes("watermark", m.keyFor(accountId, searchableId))
	if err != nil {
		return "", err
	}

	return string(b), nil
}

func (m *MetadataStorage) keyFor(accountId string, searchableId string) string {
	return cloudsearch.Md5(fmt.Sprintf("%s_%s", accountId, searchableId))
}

func (m *MetadataStorage) Close() {
	m.s.Close()
}
