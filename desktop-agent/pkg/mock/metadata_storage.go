package mock

import "github.com/pkg/errors"

type NoopMetadataStorage struct {
}

func (NoopMetadataStorage) SaveThumbnail(searchableId string, data []byte) error {
	return errors.New("noop")
}

func (NoopMetadataStorage) GetThumbnail(searchableId string) ([]byte, error) {
	return nil, errors.New("noop")
}

func (NoopMetadataStorage) WatermarkFor(accountId string, searchableId string) (string, error) {
	return "", errors.New("noop")
}

func (NoopMetadataStorage) SetWatermarkFor(accountId string, searchableId string, watermark string) error {
	return errors.New("noop")
}

func (NoopMetadataStorage) ClearWatermarks() error {
	return errors.New("noop")
}

func (NoopMetadataStorage) DeleteThumbnail(searchableId string) error {
	return errors.New("noop")
}
