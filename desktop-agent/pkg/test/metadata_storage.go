package test

import "github.com/herval/cloudsearch-desktop-agent/pkg"

type nullMetadataStorage struct {
}

func (n nullMetadataStorage) Close() {
	panic("implement me")
}

func (n nullMetadataStorage) WatermarkFor(accountId string, searchableId string) (string, error) {
	return "", nil
}

func (n nullMetadataStorage) SetWatermarkFor(accountId string, searchableId string, watermark string) error {
	return nil
}

func (n nullMetadataStorage) ClearWatermarks() error {
	return nil
}

func (n nullMetadataStorage) SaveThumbnail(searchableId string, data []byte) error {
	return nil
}

func (n nullMetadataStorage) GetThumbnail(searchableId string) ([]byte, error) {
	return nil, nil
}

func (n nullMetadataStorage) DeleteThumbnail(searchableId string) error {
	return nil
}

func NullMetadataStorage() cloudsearch.MetadataStorage {
	return nullMetadataStorage{}
}
