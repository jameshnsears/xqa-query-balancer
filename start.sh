#!/usr/bin/env bash

docker-compose -f docker-compose.yml up -d xqa-message-broker xqa-db xqa-db-amqp xqa-ingest-balancer

docker-compose -f docker-compose.yml up -d --scale xqa-shard=2

docker run -d --net="xqa-query-balancer_xqa" --name="xqa-ingest" -v $HOME/GIT_REPOS/xqa-test-data:/xml jameshnsears/xqa-ingest:latest -message_broker_host xqa-message-broker -path /xml

docker-compose logs -f xqa-shard | grep "size="
