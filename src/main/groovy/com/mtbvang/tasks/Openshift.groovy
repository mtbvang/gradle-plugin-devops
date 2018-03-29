/**
 * 
 */
package com.mtbvang.tasks

import com.mtbvang.DevtoolUtils
import com.mtbvang.OpenshiftUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Openshift {

	Logger log = LoggerFactory.getLogger(Openshift.class)

	Openshift(Project project) {
		super();
		openshiftStatus(project)
		openshiftUp(project)
		openshiftRestart(project)
		openshiftHalt(project)
		

	}

	private Task openshiftStatus(Project project) {
		project.task([group: ['Openshift']], DevtoolUtils.getPluginTaskName('openshiftStatus')) {
			doFirst {
				project.exec {
					ignoreExitValue true
					commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'oc cluster status'"
				}
			}
		}
	}

	private Task openshiftUp(Project project) {
		String description = 'Start openshift in the local development VM'
		project.task([description: description, dependsOn: ['dockerStart'], group: ['Openshift']], DevtoolUtils.getPluginTaskName('openshiftUp')) {
			doFirst {

				new ByteArrayOutputStream().withStream { os ->
					def result = project.exec {
						ignoreExitValue = true
						commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'oc cluster status'"
						standardOutput = os
					}
					def outputAsString = os.toString()
					log.info("Output of oc cluster status: \n" + outputAsString)
					def match = outputAsString.find(/The OpenShift cluster was started/)
					if (match == null ) {
						def upCommand = "oc cluster up --use-existing-config --public-hostname ${project.devtool.openshiftHostname} --host-data-dir ${project.devtool.openshiftDataDir} --host-config-dir ${project.devtool.openshiftConfigDir} "
						if(project.devtool.httpsProxy != 'null' && project.devtool.httpsProxy.trim()) {
							upCommand += "--http-proxy ${project.devtool.httpProxy} --https-proxy ${project.devtool.httpsProxy} --no-proxy ${project.devtool.noProxy} "
						}
						log.info("Openshift not running. Starting it with command: ${upCommand}")
						project.exec {
							commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c '${upCommand}'"					}
						// Give admin user cluster-admin role
						// FIXME this should be done as part of the provisioning
						project.exec {
							commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'oc login -u system:admin " \
								+ "&& oc adm policy add-cluster-role-to-user cluster-admin ${ project.devtool.openshiftDevUser }'"
						}

					} else {
						log.info("Openshift already running")
					}
				}
			}
		}
	}


	private Task openshiftRestart(Project project) {
		String description = 'Restart openshift in the local development VM. Does an openshiftHalt, openshiftUp and openshift'
		project.task([dependsOn: [
				'openshiftHalt',
				'openshiftUp'
			], group: ['Openshift'], description: description], DevtoolUtils.getPluginTaskName('openshiftRestart')) {

		}
	}

	// FIXME all openshift methods have to work with remote servers, not just vagrant.
	private Task openshiftHalt(Project project) {
		String description = 'Halt openshift running in the local development VM'
		project.task([group: ['Openshift'], description: description], DevtoolUtils.getPluginTaskName('openshiftHalt')) {
			doFirst {
				project.exec { commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'oc cluster down'"}
			}
		}
	}

}
