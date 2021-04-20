#!/bin/sh

docker run -e PORT=8080 -p 8080:8080 authgateway_prod:latest
