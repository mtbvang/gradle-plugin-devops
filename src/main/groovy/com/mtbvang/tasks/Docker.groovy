package com.mtbvang.tasks

import java.util.Map

import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.*

import com.mtbvang.DevtoolUtils

class Docker {
	Logger log = LoggerFactory.getLogger(Docker.class)

	public Docker(Project project) {
		super();

		dockerStart(project)
	}

	//FIXME This should be called vagrantDockerStart and dockerStart should start something on a remote server or just ssh into a server like AWS.
	private Task dockerStart(Project project) {
		String description = 'Starts docker in the local development VM if it is not already running.'

		project.task([group: ['Docker'], description: description], DevtoolUtils.getPluginTaskName('dockerStart')) {
			doFirst {

				new ByteArrayOutputStream().withStream { os ->
					def result = project.exec {
						ignoreExitValue = true
						commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'sudo systemctl status docker'"
						standardOutput = os
					}
					def outputAsString = os.toString()
					println("Output of docker status: \n" + outputAsString)
					def match = outputAsString.find(/Active: active/)
					if (match == null ) {
						println("Docker not running. Starting docker.")
						project.exec {
							commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c 'sudo systemctl start docker'"
						}
					} else {
						println("Docker already running")
					}
				}
			}

		}
	}
}
