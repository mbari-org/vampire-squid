#!/usr/bin/env bash

echo "--- Building vampire-squid (reminder: run docker login first!!)"

BUILD_DATE=`date -u +"%Y-%m-%dT%H:%M:%SZ"`
#VCS_REF=`git rev-parse --short HEAD`
VCS_REF=`git tag | sort -V | tail -1`

sbt pack && \
    docker build --build-arg BUILD_DATE=$BUILD_DATE \
                 --build-arg VCS_REF=$VCS_REF \
                  -t mbari/vampire-squid:${VCS_REF} \
                  -t mbari/vampire-squid:latest . && \
    docker push mbari/vampire-squid

# sbt pack && \
#     docker buildx build \
#         --platform linux/amd64,linux/arm64 \
#         -t mbari/vampire-squid:${VCS_REF} \
#         -t mbari/vampire-squid:latest \
#         --push . 
