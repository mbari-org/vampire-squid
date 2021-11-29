FROM openjdk:17-jre-slim

LABEL maintainer="Brian Schlining <bschlining@gmail.com>"

ENV APP_HOME /opt/vampire-squid

RUN mkdir -p ${APP_HOME}

COPY target/pack/ ${APP_HOME}/

EXPOSE 8080

ENTRYPOINT $APP_HOME/bin/jetty-main