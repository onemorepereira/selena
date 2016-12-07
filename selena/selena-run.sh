docker rm -f $(docker ps -aq --filter="name=$1")

docker run -d --name $1 -v $PWD/volume:/root/selena centos_selena:0.0.1 /bin/bash -c /root/selena/$2
