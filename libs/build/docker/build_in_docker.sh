#!/bin/bash

if [ $(docker ps -a | grep -c 'td_container') -eq 1 ]; then
    docker rm centos_td_stream9_arm64 -f
fi
if [ $(docker images | grep -c 'centos_td') -eq 1 ]; then
    docker image rm centos_td_stream9_arm64
fi

docker build -f Dockerfile_centos_stream9_arm64 -t centos_td_stream9_arm64 . && \
docker run -d -it --name=centos_td_stream9_arm64 -v /Users/vorobyev/Documents/projects/telegram_client:/tdlib centos_td_stream9_arm64:latest
#docker exec -it td_container /bin/bash
