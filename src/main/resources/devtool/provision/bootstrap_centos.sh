#!/bin/bash

# Remove packagekit that's causing yum to lockup.
systemctl stop packagekit
yum -y remove PackageKit
echo "> Bootstrap_centos.sh removed packagekit"

yum makecache fast

# Install epel
yum install -y wget git
wget https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
rpm -ivh epel-release-latest-7.noarch.rpm

# Work around for epel bug https://bugs.centos.org/view.php?id=13669&nbn=1
rpm -ivh --replacepkgs https://kojipkgs.fedoraproject.org/packages/http-parser/2.7.1/3.el7/x86_64/http-parser-2.7.1-3.el7.x86_64.rpm

# TODO Make swappiness and cache pressure permanent by updating /etc/sysctl.conf
# Adjust swappiness to avoid filling up swap even though there is still ram available. 
sysctl vm.swappiness=10

# Adjust for better performance
sysctl vm.vfs_cache_pressure=50

# Install require packages
yum install -y htop kdb

# Set keyboard to Danish
localectl set-keymap dk