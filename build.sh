#!/bin/sh

set -eix

cd ./desktop-agent
make
cd ..
mv desktop-agent/cloudsearch ./desktop-app/bin/
mv desktop-agent/cloudsearch.exe ./desktop-app/bin/
cd desktop-app
yarn install
yarn run build
