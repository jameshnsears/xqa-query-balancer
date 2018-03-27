# xqa-db-rest [![Build Status](https://travis-ci.org/jameshnsears/xqa-db-rest.svg?branch=master)](https://travis-ci.org/jameshnsears/xqa-db-rest) [![Coverage Status](https://coveralls.io/repos/github/jameshnsears/xqa-db-rest/badge.svg?branch=master)](https://coveralls.io/github/jameshnsears/xqa-db-rest?branch=master)
* a REST to PostgresSQL interface.

## 1. Build

### 1.1. Maven
* rm -rf $HOME/.m2/*
* mvn package -DskipTestsor

### 1.2. Docker
* docker-compose -p "dev" build --force-rm

## 2. Bring up
* docker-compose -p "dev" up -d

## 3. Test

### 3.1. Maven
* mvn clean compile test
* mvn jacoco:report coveralls:report
* mvn site  # findbugs

### 3.2. CLI
* java -jar target/xqa-db-rest-1.0.0-SNAPSHOT.jar server xqa-db-rest.yml

or

* docker-compose -p "dev" up -d

#### 3.2.1. Endpoints
* curl http://127.0.0.1:8080/search/shard/1234
* curl http://127.0.0.1:8080/status
* curl http://127.0.0.1:8080/xquery -X POST -H "Content-Type: application/json" -d '{"xqueryRequest":"count(/)"}'

* curl -X POST http://127.0.0.1:8081/tasks/log-level -H "Content-Type: application/json" -d "logger=ROOT&level=DEBUG"
* curl -X POST http://127.0.0.1:8081/tasks/gc
* curl http://127.0.0.1:8081/healthcheck
* curl http://127.0.0.1:8081/metrics

### 4. Teardown
* docker-compose -p "dev" down -v
