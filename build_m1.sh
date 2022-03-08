#!/usr/bin/env bash

echo "--- Building vampire-squid (reminder: run docker login first!!)"

VCS_REF=`git tag | sort -V | tail -1`

sbt pack \
    && docker buildx build \
        --platform linux/amd64 \
        --push \
        -t mbari/vampire-squid:${VCS_REF} \
        -t mbari/vampire-squid:latest . \
    && docker buildx build \
        --platform linux/arm64 \
        --push \
        -t mbari/vampire-squid:${VCS_REF} \
        -t mbari/vampire-squid:latest . \
    && docker buildx build \
       --platform linux/arm64 \
       --load \
       -t mbari/vampire-squid:${VCS_REF} \
       -t mbari/vampire-squid:latest .


# docker buildx build \
#        --platform linux/arm64 \
#        --load \
#        -t mbari/vampire-squid:latest .
