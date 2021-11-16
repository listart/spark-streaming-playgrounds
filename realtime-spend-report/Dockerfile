FROM maven:3.6-jdk-8-slim AS builder
COPY ./pom.xml /opt/pom.xml
COPY ./src /opt/src
RUN cd /opt; mvn clean install -Dmaven.test.skip

FROM mesosphere/spark:1.1.0-2.1.1-hadoop-2.7

COPY --from=builder /opt/target/realtime-spend-report-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/realtime-spend-report.jar

RUN cd /opt

COPY docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]