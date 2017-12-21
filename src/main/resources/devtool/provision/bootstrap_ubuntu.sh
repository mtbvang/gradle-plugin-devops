#!/bin/bash

# Install ansible
sudo apt-get install software-properties-common
sudo apt-add-repository ppa:ansible/ansible
sudo apt-get update
apt-get install -y git ansible
