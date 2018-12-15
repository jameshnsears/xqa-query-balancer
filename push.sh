#!/usr/bin/env bash

# one time password request / login
docker login -u jameshnsears

push_to_docker_hub xqa-query-balancer

docker search jameshnsears

exit $?
