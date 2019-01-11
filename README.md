# xqa-query-balancer [![Build Status](https://travis-ci.org/jameshnsears/xqa-query-balancer.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-query-balancer) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-query-balancer/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-query-balancer?branch=master) [![sonarcloud.io](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-query-balancer&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_xqa-query-balancer) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4dbb854a0f774b85898d5c36fb0a9032)](https://www.codacy.com/app/jameshnsears/xqa-query-balancer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/xqa-query-balancer&amp;utm_campaign=Badge_Grade)
* XQA REST API.

Featuring:
* SQL/JSON against xqa-db.
* XQuery against each xqa-shard and materialises the results.

## 1. Build
* ./build.sh

## 2. Test

### 2.1. Maven
* See .travis.yml

### 2.2. CLI 
* docker-compose up -d xqa-message-broker xqa-db
* docker-compose scale xqa-shard=2 
* java -jar target/xqa-query-balancer-1.0.0-SNAPSHOT.jar server xqa-query-balancer.yml

### 2.2.1. Endpoints
* curl http://127.0.0.1:9090/search/shard/a510ab7f
* curl http://127.0.0.1:9090/xquery -X POST -H "Content-Type: application/json" -d '{"xqueryRequest":"count(/)"}'

* curl -X POST http://127.0.0.1:9091/tasks/log-level -H "Content-Type: application/json" -d "logger=ROOT&level=DEBUG"
* curl -X POST http://127.0.0.1:9091/tasks/gc
* curl http://127.0.0.1:9091/healthcheck
* curl http://127.0.0.1:9091/metrics

## 3. Teardown
* docker-compose down -v

## 4. Database Connectivity
## 4.1. Empty BaseX Container database
* basexclient -U admin -P admin
* open xqa
* delete /

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
