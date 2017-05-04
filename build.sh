#!/usr/bin/env bash

echo "--- Building vampire-squid (reminder: run docker login first!!)"
sbt pack && \
  docker build -t mbari/vampire-squid . && \
  docker push mbari/vampire-squid
