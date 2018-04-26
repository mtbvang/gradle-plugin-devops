#!/bin/bash

# Setup openshift requirements
mkdir -p /openshift/data
chmod -R 0777 /openshift

# Configure openshift
sudo sysctl -w vm.max_map_count=262144

