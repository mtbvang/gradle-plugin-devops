# Gradle Devtools Plugin

Warning: work in progress.

Gradle plugin to help with local development by integrating with OpenShift, Vagrant and Docker. It's pretty tightly coupled and is being refactored. 


Usage
-----

The plugin has not been published. It can be published locally with:

```
./gradlew publishToMavenLocal
```

Build script snippet for use in all Gradle versions:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
      mavenLocal()
    }
  }
  dependencies {
    classpath "com.mtbvang:gradle-plugin-devtool:0.4.0"
  }
}

apply plugin: "devtool"

devtool {

}

// Ordering for vagrantRecreate
vagrantUp.mustRunAfter(vagrantDestroy)
// Ordering for vagrantUpMaxPower
vagrantUpMaxPower.mustRunAfter(initVagrantMaxPower)
// Ordering for vagrantRecreateMaxPower
vagrantRecreate.mustRunAfter(initVagrantMaxPower)
// Ordering for vagrantReloadMaxPower
vagrantReload.mustRunAfter(initVagrantMaxPower)
// Ordering for openshiftRestart
openshiftUp.mustRunAfter(openshiftHalt)

```

The mavenLocal() allows for local publishing of the plugin for development and testing.

All variables in var.yml become extensions and can be configured in build.gradle. A vars.yml file will be placed in the same folder as the build.gradle file on first run if one does not exist. 


### Configuration

The plugin requires a var.yml file in the root folder ofhe project. It will place default ones in there if one does not exist.


Development
-----------

Tests can be run with the following commands

´´´
./gradlew functionalTest -PfunctionalTestEnabled=true

´´´

Functional test with filtering of only specific tests with info:

´´´
./gradlew functionalTest -PfunctionalTestEnabled=true --tests *VagrantTest.vagrantUp -i
´´´

### Debugging

Debugging is turned on by:

- Uncommenting the org.gradle.jvmargs property in gradle.properties
- Adding a remote debugger in your IDE listening on the address value of org.gradle.jvmargs e.g. 5011
- Appending --debug to the test run e.g.:

```
./gradlew functionalTest -PfunctionalTestEnabled=true --tests *VagrantTest.vagrantUp --debug
```

More information about test filtering https://docs.gradle.org/current/userguide/java_plugin.html#test_filtering

[here]:https://plugins.gradle.org/plugin/com.mtbvang.devtool