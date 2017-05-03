#!/usr/bin/env bash

echo "--- Building vampire-squid (reminder: run docker login first!!)"
sbt pack && \
  docker build -t hohonuuli/vampire-squid . && \
  docker push hohonuuli/vampire-squid
