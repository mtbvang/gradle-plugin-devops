#!/bin/bash

PROJECT_NAME=$1
ANSIBLE_GALAXY_FORCE=$2 # Note This variable has no effect. Even with force set the transitive dependencies are not being updated. Doing a delete of roles instead.

echo "PROJECT_NAME=$PROJECT_NAME"
echo "ANSIBLE_GALAXY_FORCE=$ANSIBLE_GALAXY_FORCE"

# Run ansibile
# Note vagrant runs as root, but pwd is set to /home/vagrant
cd /vagrant/$PROJECT_NAME
# Force removal of roles since --force does not work
rm -rf provision/roles
ansible-galaxy install -p provision/roles -c -v -r provision/requirements.yml $ANSIBLE_GALAXY_FORCE