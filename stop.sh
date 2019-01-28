#!/usr/bin/env bash

docker rm -f xqa-ingest cadvisor

docker-compose down -v
