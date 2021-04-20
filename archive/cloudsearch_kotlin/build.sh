#!/bin/sh

gradle embedded:shadowJar &&
	cp embedded/build/libs/cloudsearch.jar ~/Dropbox/dev/cloudsearch-bin
	cp embedded/build/libs/cloudsearch.jar ~/Development/cloudsearch/desktop-app/bin
