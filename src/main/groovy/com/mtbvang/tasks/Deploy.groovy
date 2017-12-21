/**
 * 
 */
package com.mtbvang.tasks

import com.hubspot.jinjava.Jinjava
import com.mtbvang.DeployUtils
import com.mtbvang.DevtoolUtils

import java.util.Map

import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.*
import org.yaml.snakeyaml.Yaml

/**
 * @author Vang Nguyen
 *
 */
//@Slf4j
class Deploy {

	Logger log = LoggerFactory.getLogger(Deploy.class)

	public Deploy(Project project) {
		super();
		downloadFromNexus(project)
		deployAll(project)
		deployAllFromNexus(project)
		undeployAll(project)
		deployApp(project)
		deployFromNexus(project)
		undeployApp(project)
		deployFromNexusInit(project)
	}

	private void downloadFromNexus(Project project) {
		project.task(DevtoolUtils.getPluginTaskName('downloadFromNexus')) {
			doLast {

				DevtoolUtils.downloadFromNexus(project, true,
						project.devtool.appName,
						project.devtool.artifactId,
						project.devtool.version,
						project.devtool.packaging,
						project.devtool.groupId)
			}
		}
	}

	private void deployAllFromNexus(Project project) {
		project.task([group: ['Deployment'], dependsOn: ['deployFromNexusInit']], DevtoolUtils.getPluginTaskName('deployAllFromNexus')) {
			doLast {
				log.info("project devtool: {}", project.devtool.toString())
				project.devtool.apps.each { appConfig ->
					if (appConfig.availableInNexus != null && !appConfig.availableInNexus) {
						return
					}

					DevtoolUtils.enableApp(project, appConfig.name)
					DevtoolUtils.setNexusBuildPath(project, appConfig.name)
					DevtoolUtils.downloadFromNexus(project, appConfig.enabled, appConfig.name, appConfig.artifactId, appConfig.version, appConfig.packaging, appConfig.groupId)
				}
				DeployUtils.runLocalDevPlaybookViaVagrant(project)
				//				project.ext.runLocalDevPlaybookViaVagrant()
				//				project.devtool.apps.each { appConfig ->
				//					if (appConfig.get("availableInNexus") != null && !appConfig.get("availableInNexus")) {
				//						return
				//					}
				// Check if port should be forwarded
				//					project.ext.openshiftPortForwardFromHost(appConfig.name)
				//				}
			}
		}
	}

	private Task deployFromNexus(Project project) {

		Task newTask
		project.devtool.apps.each { appConfig ->
			if (appConfig.availableInNexus != null && !appConfig.availableInNexus) {
				return
			}
			newTask = project.task([group: ['Deployment'], dependsOn: ['deployFromNexusInit']], DevtoolUtils.getPluginTaskName("Deploy-${appConfig.name}-from-nexus")) {
				doLast {
					
					def projectEnabled = "${appConfig.name}Enabled".camelCase()
					project.devtool."${projectEnabled}" = true
					def projectBuildPath = "${appConfig.name}BuildPath".camelCase()
					project.devtool."${projectBuildPath}" = 'target'
					def projectVersion = "${appConfig.name}Version".camelCase()
					project.devtool."${projectVersion}" = 
					//					project.ext."${projectVersion}" = project.devtool."${projectVersion}"
					//					project.ext.downloadFromNexus(project.ext.("${appConfig.name}Enabled".camelCase()), appConfig.name, appConfig.get("artifactId"), project.ext."${projectVersion}", appConfig.get("packaging"), appConfig.get("groupId"))
					DeployUtils.runLocalDevPlaybookViaVagrant()
					//					project.ext.openshiftPortForwardFromHost(appConfig.name)
				}
			}
		}
		return newTask
	}

	private Task deployAll(Project project) {
		String description = "Builds and deploys all applications to openshift running in openshiftHostname. Run from host."
		project.task([group: ['Deployment'], dependsOn: ['deployInit'], description: description], DevtoolUtils.getPluginTaskName('DeployAll')) {

			doLast {
				project.devtool.apps.each { appConfig ->
					//					project.ext."${appConfig.name.camelCase()}Enabled" = true
				}
				//				project.ext.runLocalDevPlaybookViaVagrant()
				//				project.devtool.apps.each { appConfig ->
				//					project.ext.openshiftPortForwardFromHost(appConfig.name)
				//				}
			}
		}
	}

	private Task deployApp(Project project) {

		Task newTask
		project.devtool.apps.each { appConfig ->
			String taskName = DevtoolUtils.getPluginTaskName("deploy-${appConfig.name}")
			String description = "Builds and deploys ${appConfig.name} to openshift running in openshiftHostname. Run from host."

			newTask = project.task([group: ['Deployment'], dependsOn: ['deployInit'], description: description], taskName) {
				doLast {
					//					project.ext.("${appConfig.name}Enabled".camelCase()) = true
					//					project.ext.runLocalDevPlaybookViaVagrant()
					//					project.ext.openshiftPortForwardFromHost(appConfig.name)
				}
			}
		}

		newTask

	}

	private Task undeployAll(Project project) {
		String description = 'Undeploys all apps from openshift'

		project.task([group: ['Deployment'], description: description], DevtoolUtils.getPluginTaskName('undeployAll')) {
			doLast { DevtoolUtils.undeployAllApps(project) }
		}
	}

	private Task undeployApp(Project project) {
		Task newTask
		project.devtool.apps.each { appConfig ->
			String taskName = DevtoolUtils.getPluginTaskName("undeploy-${appConfig.name}")
			String description = "Undeploys ${appConfig.name} from openshift"

			newTask = project.task([group: ['Deployment'], description: description], taskName) {
				doLast {
					DevtoolUtils.undeployApp(project, appConfig.name)
				}
			}
		}
		newTask

	}

	private Task deployFromNexusInit(Project project) {
		String description = 'Sets initialization conditions for tasks wanting to deploy to nexus'
		project.task([description: description], DevtoolUtils.getPluginTaskName('deployFromNexusInit')) {
			doFirst {
				project.devtool.cloneAppsCode = false
				project.devtool.buildProjects = false
				project.devtool.ansibleWorkDir = "${project.devtool.guestProjectDir}/build"
			}
		}
	}
}
