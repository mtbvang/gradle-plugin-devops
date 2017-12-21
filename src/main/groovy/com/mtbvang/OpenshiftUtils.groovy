package com.mtbvang

import org.gradle.api.Task
import org.slf4j.*
import org.gradle.api.Project
import org.apache.commons.lang3.SystemUtils

import static java.util.concurrent.TimeUnit.SECONDS
import static org.awaitility.Awaitility.*

public final class OpenshiftUtils {


	private static Logger log = LoggerFactory.getLogger(OpenshiftUtils.class)


	public static portForwardAll(Project project) {
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

			project.devtool.apps.each { portForwardApp(project, it.name) }
		} else {
			println "Not port forwarding. Port forwarding only done for local host 127.0.0.1"
		}
	}

	public static portForwardApp(Project project, String appName) {
		String description = 'Setup port forwarding to access services running in openshift from host.'

		if (!project.devtool.openshiftHostname.contains('127.0.0.1') && !project.devtool.openshiftHostname.contains('localhost')) {
			println("Skipping port-forward as we are deploying to ${project.devtool.openshiftHostname}. We only port-forward to localhost")
			return
		}

		println("Port forwarding app: " + appName)

		if (isAppEnabled(project, appName)) {
			def openshiftProjectName = project.devtool.openshiftProject
			def openshiftHostname = project.devtool.openshiftHostname
			def openshiftPort = project.devtool.openshiftPort
			def openshiftUser = project.devtool.openshiftDevUser
			def openshiftPassword = project.devtool.openshiftDevPassword

			if(isOpenshiftProject(project, openshiftProjectName, openshiftHostname, openshiftPort, openshiftUser, openshiftPassword)) {
				openshiftPortForward(project, appName)
			}else {
				assert false : "Timed out waiting for Openshift pods to start"
			}
		} else {
			println "Not port forwarding. ${ appName } enabled: " + isAppEnabled(appName)
		}

	}

	/*
	 * Wait for openshift to startup and check if the project exists.
	 */
	private static boolean isOpenshiftProject(def project, def openshiftProjectName, def openshiftHostname, def openshiftPort, def openshiftUser, def openshiftPassword) {
		def isOpenshiftRunning = false
		def outputAsString = ""

		int timeoutInSeconds = 120

		await().ignoreExceptions().atMost(timeoutInSeconds, SECONDS).until({
			while(isOpenshiftRunning == false) {
				new ByteArrayOutputStream().withStream { os ->
					def command
					def match
					// Try to log in
					if (isOpenshiftRunning == false) {
						ocLogin(project, openshiftHostname, openshiftPort, openshiftUser, openshiftPassword)
						isOpenshiftRunning = true
					}

					// Switch to project
					project.exec {
						ignoreExitValue = true
						command = "oc project ${openshiftProjectName}"
						commandLine "bash", "-c", command
						errorOutput = os
					}
					outputAsString = os.toString()
					match = outputAsString.find(/error:/)
					if(match == null) {
						println("${openshiftProjectName} exists: ${outputAsString}")
						return true
					}else {
						return false
					}
				}
			}
		})
	}

	private static boolean isOpenshiftRunning(def project, def openshiftProjectName, def openshiftHostname, def openshiftPort, def openshiftUser, def openshiftPassword) {
		int timeoutInSeconds = 120
		
		await().ignoreExceptions().atMost(timeoutInSeconds, SECONDS).until({
			while(isOpenshiftRunning == false) {
				new ByteArrayOutputStream().withStream { os ->
					def command
					def match
					// Check if openshift is running by doing an oc status
					if (isOpenshiftRunning == false) {
						ocLogin(project, openshiftHostname, openshiftPort, openshiftUser, openshiftPassword)
						isOpenshiftRunning = true
					}
				}
			}
		})

	}

	private static Boolean isAppEnabled(Project project, String appName) {
		def projectEnabledVar = (appName + 'Enabled').camelCase()
		log.info "isAppEnabled: " + project.devtool."${projectEnabledVar}"
		return project.devtool."${projectEnabledVar}".toBoolean()
	}

	private static Boolean openshiftPortForward(Project project, String appName) {

		def outputAsString = ""
		def match
		def command

		def serviceport = project.devtool."${(appName + 'Port').camelCase()}"
		log.info("serviceport: {}", serviceport)
		def managePort = project.devtool."${(appName + 'PortManage').camelCase()}"
		def jrebelPort = project.devtool."${(appName + 'PortJrebel').camelCase()}"
		def jmxPort = project.devtool."${(appName + 'PortJmx').camelCase()}"
		def remoteDebuggingPort = project.devtool."${(appName + 'PortRemotedebugging').camelCase()}"
		println """
			${appName} port(s): 
			service: ${serviceport} 
			manage: ${managePort}
			jmx: ${jmxPort} 
			jrebel: ${jrebelPort} 
			remote debug: ${remoteDebuggingPort}"""

		int[] ports = [
			serviceport,
			managePort,
			jrebelPort,
			jmxPort,
			remoteDebuggingPort
		]

		def openshiftHostname = project.devtool.openshiftHostname
		def openshiftPort = project.devtool.openshiftPort
		def openshiftUser = project.devtool.openshiftDevUser
		def openshiftPassword = project.devtool.openshiftDevPassword

		// Do the actual port forwarding. Only forward port for pods that are running.
		new ByteArrayOutputStream().withStream { os ->
			ocLogin(project, openshiftHostname, openshiftPort, openshiftUser, openshiftPassword)
			ocProject(project, openshiftProjectName)
			isPodRunning(project, appName, os)

			await().ignoreExceptions().atMost(openshiftPortforwardWait, SECONDS).until({
				if(!isPodRunning(project, appName, os)) {
					println("Pod for ${appName} is not running. Not forwarding ports ${ports}\n")
					sleep(3000)
				}else {
					println("Pod for ${appName} is running. Forwarding ports ${ports}\n")
					def expandedPorts = ports.join(' ')
					project.exec {

						ignoreExitValue = true
						commandLine "bash", "-c", """((nohup oc port-forward \$(oc get pods | grep ${appName} | grep -i running | grep -v build | grep -v deploy | awk '{print \$1}') ${expandedPorts} 1>/dev/null 2>&1 &)&)"""
						//standardOutput = os
						errorOutput = os
					}
					outputAsString = os.toString()
					println("Port forward output: \n" + outputAsString)
					os.reset()

					await().atMost(openshiftPortforwardWait, SECONDS).until({
						if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
							project.exec {
								ignoreExitValue = true
								command = "ps x | grep '[p]ort-forward ${appName}'"
								println("command: " + command)
								commandLine "bash", "-c", command
								standardOutput = os
							}
							outputAsString = os.toString()
							println("Result of grep for ${appName} port forward: \n" + outputAsString)
							def pattern = "port-forward ${appName}"
							match = outputAsString.find(/${pattern}/)
							println("match: "+  match)
							return match != null && !match.equals("")
						} else {
							exec {
								ignoreExitValue = true
								command = scriptOf(getPortFowardCommandLineFor(appName))
								println("command: " + command)
								commandLine "bash", "-c", command
								standardOutput = os
							}
							outputAsString = os.toString()
							return outputAsString != null && !outputAsString.equals("")
						}
					})
					return true
				}
			})
			println("***************************************************** \n")
			return false
		}
	}

	public static ocLogin(def project, def openshiftHostname, def openshiftPort , def username, def password) {
		// TODO add checking
		project.exec {
			commandLine "bash", "-c", "oc login --insecure-skip-tls-verify=true -u ${ username } -p ${ password } https://${openshiftHostname}:${openshiftPort}"
		}
	}

	public static ocProject(Project project, String openshiftProjectName) {
		project.exec {
			ignoreExitValue = true
			commandLine "bash", "-c", "oc project ${ openshiftProjectName }"
		}
	}

	public static boolean isPodRunning(Project project, String appName, OutputStream os) {
		// Check if pod for component is running
		project.exec {
			ignoreExitValue = true
			command = "oc get pods --show-all=false | grep -i '${appName}' | grep 'Running'"
			commandLine "bash", "-c", command
			standardOutput = os
		}

		println("Result of checking for component pod running: " + os.toString())
		def match = os.toString().find(/Running/)
		if(match == null) {
			false
		}else {
			true
		}
	}

}
