FROM ubuntu:bionic

RUN apt-get -qq update
RUN apt-get -qq install -y openjdk-11-jre

ARG OPTDIR=/opt
ARG XQA=/xqa-query-balancer

RUN mkdir -p ${OPTDIR}${XQA}
COPY target/xqa-query-balancer-1.0.0-SNAPSHOT.jar ${OPTDIR}${XQA}
COPY xqa-query-balancer.yml ${OPTDIR}${XQA}

RUN useradd -r -M -d ${OPTDIR}${XQA} xqa
RUN chown -R xqa:xqa ${OPTDIR}${XQA}

USER xqa

WORKDIR ${OPTDIR}${XQA}

ENTRYPOINT ["java", "-jar", "xqa-query-balancer-1.0.0-SNAPSHOT.jar", "server", "xqa-query-balancer.yml"]

EXPOSE 8080 8081
