package com.mtbvang

import org.gradle.api.Task
import org.slf4j.*
import org.gradle.api.Project
import org.apache.commons.lang3.SystemUtils

public final class DevtoolUtils {
	private static final String taskPrefix = ''

	private static Logger log = LoggerFactory.getLogger(DevtoolUtils.class)

	/*
	 * Method required because of use of taskPrefix
	 */
	public static String getPluginTaskName(String taskName) {

		String pluginTaskName = "${taskPrefix}${taskName}".camelCase()
		log.debug("getPluginTaskName returning: {}", pluginTaskName)
		pluginTaskName
	}

	/*
	 * Sets both the top level variable that controls if an app is enabled and the enabled property with in the apps list.
	 */
	public static String enableApp(Project project, String appName) {
		project.devtool."${(appName + 'Enabled').camelCase()}" = true
		project.devtool.apps.find({ it.name == appName }).enabled = true
		log.debug("{}, enabled: {}", appName, project.devtool.apps.find({ it.name == appName }).enabled)
		log.debug("{}, project.devtool enabled: {}", appName, project.devtool."${(appName + 'Enabled').camelCase()}")
	}

	public static String setNexusBuildPath(Project project, String appName) {
		project.devtool."${(appName + 'BuildPath').camelCase()}" = 'target'
		project.devtool.apps.find({ it.name == appName }).buildPath = 'target'
		log.debug("{}, apps build path: {}", appName, project.devtool.apps.find({ it.name == appName }).buildPath)
		log.debug("{}, project.devtool build path: {}", appName, project.devtool."${(appName + 'BuildPath').camelCase()}")
	}

	public static void downloadFromNexus(Project project, Boolean shouldDownload, String appNameParam, String artifactIdParam, String versionParam, String packagingParam, String groupIdParam) {
		if(!shouldDownload) {
			log.info("Skipping download of ${appNameParam} from Nexus")
			return
		}

		project.exec {
			def command = "mvn org.apache.maven.plugins:maven-dependency-plugin:2.9:get " \
				+ "-Dartifact='${groupIdParam}:${artifactIdParam}:${versionParam}:${packagingParam}' " \
				+ "-Ddest='build/${appNameParam}/${project.devtool.buildPathDefault}/${artifactIdParam}-${versionParam}.${packagingParam}' -U"
			log.info(command)
			commandLine "bash", "-c", command
		}
	}

	public static void downloadFromNexus(Project project) {
		downloadFromNexus(project, true, project.devtool.appName, project.devtool.artifactId, project.devtool.version, project.devtool.packaging, project.devtool.groupId)
	}


	public static String undeployApp(Project project, String appName) {
		String description = 'Undeploys the specified app from openshift'
		project.exec {
			commandLine "bash", "-c", "oc delete all -l app=${appName}"
		}
	}

	public static String undeployAllApps(Project project) {
		String description = 'Undeploys all apps from openshift'
		project.devtool.apps.each { undeployApp(project, it.name) }
	}

	public static portForward(Project project) {
		String description = 'Setup port forwarding to access services running in openshift from host.'

		// Only portforward for local development environment
		if(project.devtool.openshiftHostname == '127.0.0.1') {
			def killcommand = "kill \$(ps aux | grep 'port-forward' | awk '{print \$2}')"
			if (SystemUtils.IS_OS_WINDOWS) {
				killcommand = "kill \$(ps aux | grep 'openshiftclient' | awk '{print \$1}')"
			}

			project.exec {
				ignoreExitValue = true
				commandLine "bash", "-c", killcommand
			}

			project.devtool.apps.each { OpenshiftUtils.portForwardApp(project, it.name) }
		} else {
			println "Not port forwarding. Port forwarding only done for local host 127.0.0.1"
		}


	}

}
