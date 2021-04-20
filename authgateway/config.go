package authgateway

import (
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"path/filepath"
)

type Config struct {
	BaseUrl   string     `yaml:"baseUrl"`
	Secret    string     `yaml:"secret"`
	Providers []*Provider `yaml:"providers"`
}

func (c *Config) ProviderFor(n string) *Provider {
	for _, p := range c.Providers {
		if p.Name == n {
			return p
		}
	}

	return nil
}

func ParseConfig(file string) (*Config, error) {
	filename, _ := filepath.Abs(file)
	yamlFile, err := ioutil.ReadFile(filename)

	if err != nil {
		return nil, err
	}

	var config Config

	err = yaml.Unmarshal(yamlFile, &config)
	if err != nil {
		return nil, err
	}

	for _, p := range config.Providers {
		err = p.Parse(config)
		if err != nil {
			return nil, err
		}
	}

	return &config, nil
}
