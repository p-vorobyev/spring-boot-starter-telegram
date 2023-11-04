#!/bin/bash

if [ $(docker ps -a | grep -c 'td_container') -eq 1 ]; then
    docker rm td_container -f
fi
if [ $(docker images | grep -c 'centos_td') -eq 1 ]; then
    docker image rm centos_td
fi

docker build -f Dockerfile_centos -t centos_td . && \
docker run -d -it --name=td_container -v /Users/vorobyev/Documents/projects/td:/tdlib centos_td:latest
#docker exec -it td_container /bin/bash