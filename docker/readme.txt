mkdir td-1.8.13 && cd td-1.8.13 && git clone https://github.com/tdlib/td.git && cd .. && \
docker build -f Dockerfile_centos -t centos_td_1.8.13 . && \
docker run -d -it --name=td -v "$(pwd)"/td-1.8.13:/td-1.8.13 centos_td_1.8.13:latest && \
docker exec -it td /bin/bash