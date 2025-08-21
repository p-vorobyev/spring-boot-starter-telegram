#!/bin/sh

sudo yum update -y
sudo dnf --enablerepo=powertools install -y gperf
sudo yum install -y gcc-c++ make git zlib-devel openssl-devel php cmake java-11-openjdk-devel