# Gradle Devtools Plugin

Warning: work in progress.

Gradle plugin to help with local development by integrating with OpenShift, Vagrant and Docker. It's pretty tightly coupled and is being refactored. 

[![Build Status](https://travis-ci.org/mtbvang/gradle-plugin-devtool.svg?branch=master)](https://travis-ci.org/mtbvang/gradle-plugin-devtool)

Latest Version
--------------
All versions can be found [here].

Usage
-----------

Build script snippet for use in all Gradle versions:
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
      mavenLocal()
    }
  }
  dependencies {
    classpath "com.mtbvang:gradle-plugin-devtool:0.1.0"
  }
}

apply plugin: "devtool"
```

The mavenLocal() allows for local publishing of the plugin for development and testing.

Tasks
-----------


```

## Configuration

The plugin requires a var.yml file and apps.yml file in the root folder ofhe project. It will place default ones in there if one does not exist.

All variables in var.yml become extensions and can be configured in build.gradle.

#### build.gradle
```groovy
devtool {

}
```

[here]:https://plugins.gradle.org/plugin/com.mtbvang.devtool