#!/bin/bash

mkdir intelij
tar -xvf Downloads/*.tar.gz -C intelij/
ln -s ./intelij/*/bin/idea.sh ./idea.sh

sudo apt update
sudo apt-get install -y maven