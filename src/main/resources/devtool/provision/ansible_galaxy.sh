#!/bin/bash

# Used by vagrant file and can also become run independently.

# Parameters with default values
ROLES_PATH=${1:-'./'}    
ANSIBLE_GALAXY_FORCE=${2:-'--force'} # Note This variable has no effect. Even with force set the transitive dependencies are not being updated. Doing a delete of roles instead.

# Run ansibile
# Note vagrant runs as root, but pwd is set to /home/vagrant
cd $ROLES_PATH
# Force removal of roles since --force does not work
rm -rf roles
ansible-galaxy install -p roles -c -v -r requirements.yml $ANSIBLE_GALAXY_FORCE