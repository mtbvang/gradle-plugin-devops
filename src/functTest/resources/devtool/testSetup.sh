#!/bin/bash

export VM_NAME=$1
export VM_MEMORY=$2
export VM_GUI=$3
export VM_CPUS=$4
export VM_CPU_CAP=$5
export OPENSHIFT_PORT_HOST=$6

# Setup openshift requirements
vagrant up centos --no-provision
