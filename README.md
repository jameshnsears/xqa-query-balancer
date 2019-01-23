# xqa-query-balancer [![Build Status](https://travis-ci.org/jameshnsears/xqa-query-balancer.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-query-balancer) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-query-balancer/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-query-balancer?branch=master) [![sonarcloud.io](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-query-balancer&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_xqa-query-balancer) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4dbb854a0f774b85898d5c36fb0a9032)](https://www.codacy.com/app/jameshnsears/xqa-query-balancer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/xqa-query-balancer&amp;utm_campaign=Badge_Grade)
* XQA REST API.

Featuring:
* SQL/JSON against xqa-db.
* Materialised XQuery against xqa-shard(s).

## 1. Build
* ./build.sh

## 2. Test

### 2.1. Maven
* See .travis.yml

### 2.2. CLI 
* populate xqa-shard(s) and xqa-db using xqa-test-data:
```
docker-compose up -d xqa-message-broker xqa-db xqa-db-amqp xqa-ingest-balancer

docker-compose up -d --scale xqa-shard=2

docker run -d --net="xqa-query-balancer_xqa" --name="xqa-ingest" -v $HOME/GIT_REPOS/xqa-test-data:/xml jameshnsears/xqa-ingest:latest -message_broker_host xqa-message-broker -path /xml
```
* wait until data in xqa:
    * docker logs xqa-ingest | grep "FINISHED - sent: 40/40"
    * docker-compose logs xqa-shard | grep "size="
```
docker run -d --net="xqa-query-balancer_xqa" --name="xqa-query-balancer" -p9090:9090 -p9091:9091 xqa-query-balancer 
```

### 2.2.1. Search
```
{
"searchResponse":
    [
        {
            "creationTime":"2019-01-22 12:03:22.919+00",
            "serviceId":"/xml/DBER-1923-0416.xml",
            "subject":"aa84010cfefca52e93b61d528a4e869b0cc7b051fd627a072c0b38857d97d8b5",
            "digest":"ingest/8d9d6ed5"
        }
    ]
}
```

* curl http://127.0.0.1:9090/search
* curl http://127.0.0.1:9090/search/filename/DBER-1923-0416.xml
* curl http://127.0.0.1:9090/search/digest/d6f04c988162284ff57c06e69
* curl http://127.0.0.1:9090/search/service/ingest

### 2.2.2. XQuery
```
{
"xqueryResponse":"<xqueryResponse>\n<shard id='26507201'>\n20\n</shard><shard id='dd929cc5'>\n20\n</shard></xqueryResponse>"
}
```
* curl http://127.0.0.1:9090/xquery -X POST -H "Content-Type: application/json" -d '{"xqueryRequest":"count(/)"}'

### 2.2.3. Admin
* curl http://127.0.0.1:9091/healthcheck
* curl http://127.0.0.1:9091/metrics
* curl -X POST http://127.0.0.1:9091/tasks/log-level -H "Content-Type: application/json" -d "logger=ROOT&level=DEBUG"
* curl -X POST http://127.0.0.1:9091/tasks/gc

## 3. Teardown
* docker-compose down -v

## 4. Useful storage commands
## 4.1. BaseX
```
docker ps -a
CONTAINER ID        IMAGE                                     COMMAND                  CREATED             STATUS                      PORTS                                                                 NAMES
ccb338c21e36        jameshnsears/xqa-shard:latest             "python3 xqa/shard.p…"   28 seconds ago      Up 25 seconds               0.0.0.0:32778->1984/tcp                                               xqa-query-balancer_xqa-shard_1_ab193b02ad89
fc1566caa4e5        jameshnsears/xqa-shard:latest             "python3 xqa/shard.p…"   28 seconds ago      Up 25 seconds               0.0.0.0:32777->1984/tcp                                               xqa-query-balancer_xqa-shard_2_ec9ef595b8d0

basexclient -U admin -P admin -p 32778
list # nothing will show when using: -storage_mainmem
open xqa

```

## 4.2. psql
```
psql -h 0.0.0.0 -p 5432 -U xqa

select  distinct to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime,
        info->>'source' as filename,
        info->>'digest' as digest,
        info->>'serviceId' as service
from events
where filename like '%/xml/DAQU-1931-0321.xml%'
order by creationTime asc;
```
