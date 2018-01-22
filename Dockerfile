FROM debian:stretch

MAINTAINER james.hn.sears@gmail.com

RUN apt-get -qq update
RUN apt-get -qq install -y openjdk-8-jre

ARG OPTDIR=/opt
ARG XQA=/xqa-db-rest

RUN mkdir -p ${OPTDIR}${XQA}
COPY target/xqa-db-rest-1.0.0-SNAPSHOT.jar ${OPTDIR}${XQA}
COPY xqa-db-rest.yml ${OPTDIR}${XQA}

RUN useradd -r -M -d ${OPTDIR}${XQA} xqa
RUN chown -R xqa:xqa ${OPTDIR}${XQA}

USER xqa

WORKDIR ${OPTDIR}${XQA}

ENTRYPOINT ["java", "-jar", "xqa-db-rest-1.0.0-SNAPSHOT.jar", "server", "xqa-db-rest.yml"]
