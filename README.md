# xqa-query-balancer [![Build Status](https://travis-ci.org/jameshnsears/xqa-query-balancer.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-query-balancer) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-query-balancer/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-query-balancer?branch=master) [![sonarcloud.io](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-query-balancer&metric=alert_status)](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_xqa-query-balancer&metric=alert_status)
* REST API interface:
    * SQL/JSON against xqa-db.
    * XQuery against each xqa-shard and materialises the results.

## 1. Build
* ./build.sh

## 2. Bring up
* docker-compose up -d xqa-db xqa-message-broker xqa-ingest-balancer
* docker-compose up -d --scale xqa-shard=2

## 3. Test

### 3.1. Maven
* mvn test
* mvn jacoco:report coveralls:report
* mvn site  # findbugs

### 3.2. CLI
* java -jar target/xqa-query-balancer-1.0.0-SNAPSHOT.jar server xqa-query-balancer.yml

or

* docker-compose up -d

#### 3.2.1. Endpoints
* curl http://127.0.0.1:8080/search/shard/a510ab7f
* curl http://127.0.0.1:8080/xquery -X POST -H "Content-Type: application/json" -d '{"xqueryRequest":"count(/)"}'

* curl -X POST http://127.0.0.1:8081/tasks/log-level -H "Content-Type: application/json" -d "logger=ROOT&level=DEBUG"
* curl -X POST http://127.0.0.1:8081/tasks/gc
* curl http://127.0.0.1:8081/healthcheck
* curl http://127.0.0.1:8081/metrics

## 4. Teardown
* docker-compose down -v

## 5. Empty BaseX Container database
* basexclient -U admin -P admin
* open xqa
* delete /
