FROM openjdk:17

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION
LABEL org.label-schema.build-date=$BUILD_DATE \
  org.label-schema.name="vampire-squid" \
  org.label-schema.description="A RESTful microservice-based video asset manager" \
  org.label-schema.url="https://mbari-media-management.github.io/" \
  org.label-schema.vcs-ref=$VCS_REF \
  org.label-schema.vcs-url="https://github.com/mbari-media-management/vampire-squid" \
  org.label-schema.vendor="Monterey Bay Aquarium Research Institute" \
  org.label-schema.schema-version="1.0" \
  maintainer="Brian Schlining <brian@mbari.org>"

ENV APP_HOME /opt/vampire-squid

RUN mkdir -p ${APP_HOME}

COPY target/pack/ ${APP_HOME}/

EXPOSE 8080

ENTRYPOINT $APP_HOME/bin/jetty-main