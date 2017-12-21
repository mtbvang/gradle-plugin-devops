/**
 * 
 */
package com.mtbvang.tasks

import java.util.Map

import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.mtbvang.DevtoolUtils
import com.mtbvang.OpenshiftUtils


class Openshift {

	Logger log = LoggerFactory.getLogger(Openshift.class)

	public Openshift(Project project) {
		super();
		openshiftStatus(project)
		openshiftUp(project)
		openshiftPortForwardApp(project)
		openshiftPortForwardAll(project)
		openshiftRestart(project)
		openshiftHalt(project)
		
		// Ordering for openshiftRestart
		project.getTaskByPluginName('openshiftUp').shouldRunAfter(project.getTaskByPluginName('openshiftHalt'))
		project.getTaskByPluginName('openshiftPortForwardAll').shouldRunAfter(project.getTaskByPluginName('openshiftUp'))

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
						log.info("Openshift not running. Starting it.")
						project.exec {
							commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'oc cluster up --http-proxy ${project.devtool.httpProxy} " \
								+ "--https-proxy ${project.devtool.httpsProxy} --no-proxy ${project.devtool.noProxy} " \
								+ "--public-hostname ${project.devtool.openshiftHostname} --host-data-dir ${project.devtool.openshiftDataDir}'"
						}
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

				// Test openshift is working with a curl
				new ByteArrayOutputStream().withStream { os ->
					def result = project.exec {
						commandLine "bash", "-c", "oc login --insecure-skip-tls-verify=true -u ${project.devtool.openshiftDevUser} -p ${project.devtool.openshiftDevPassword} https://${project.devtool.openshiftHostname}:${project.devtool.openshiftPort} && oc get projects"
						standardOutput = os
					}
					def outputAsString = os.toString()
					log.info(outputAsString)
					def match = outputAsString.find(/Login successful/)
					assert match != null : "Login failed."
				}

			}
		}
	}


	private Task openshiftPortForwardApp(Project project) {
		Task newTask
		project.devtool.apps.each { projectConfig ->
			newTask = project.task([group: ['Openshift']], DevtoolUtils.getPluginTaskName("openshiftPortForward-${projectConfig.name}")) {
				log.info("Task created: ${this}")
				doLast {
					project.devtool.("${projectConfig.name}Enabled".camelCase()) = true
					OpenshiftUtils.portForwardApp(project, projectConfig.name)
				}
			}
		}
		return newTask
	}

	private Task openshiftPortForwardAll(Project project) {
		project.task([group: ['Openshift']], DevtoolUtils.getPluginTaskName("openshiftPortForwardAll")) {
			doLast {
				project.ext.enableAllProjects()
				project.ext.openshiftPortForwardAllFromHost()
			}
		}
	}

	private Task openshiftRestart(Project project) {
		String description = 'Restart openshift in the local development VM. Does an openshiftHalt, openshiftUp and openshift'
		project.task([dependsOn: [
				'openshiftHalt',
				'openshiftUp',
				'openshiftPortForwardAll'
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
