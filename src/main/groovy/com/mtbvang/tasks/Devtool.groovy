/**
 * 
 */
package com.mtbvang.tasks

import com.hubspot.jinjava.Jinjava
import com.mtbvang.DevtoolUtils

import java.util.Map

import org.gradle.api.Project
import org.gradle.api.Task
import org.yaml.snakeyaml.Yaml
import org.apache.commons.lang3.SystemUtils

/**
 * @author Vang Nguyen
 *
 */
class Devtool {

	public Devtool(Project project) {
		super();
		
		// Create all tasks before specifying dependencies to avoid null pointers.
		up(project)
		destroy(project)
		recreate(project)
		recreateMaxPower(project)
		reload(project)
		reloadMaxPower(project)
		upMaxPower(project)
		bashSetup(project)
		
	}

	private Task up(Project project) {
		String description = "DEFAULT TASK: Bring up and provision the local development VM. Runs vagrantInstallPlugins, vagrantUp, updateHostsFile, openshiftUp, openshiftPortForwardAll, deploy. Because of openshiftPortForwardAll task, this task can only be run from the host.\n" \
			+ "* vagrantVMName	  	- String - Default: centos - The name of the vagrant VM that commands apply to.\n" \
			+ "* vagrantProvider	- String - Default: virtualbox - The virutal machine provider to bring up the VM.\n" \
			+ "* vagrantGui		 	- true|false - Default: true - Whether or not to show the VM GUI.\n" \
			+ "* runAnsibleGalaxy	 - true|false - Default: false - Whether or not vagrant spins up the VM in testing mode. Look at what is being done with the TESTING variable in Vagrantfile.\n" \
			+ "* vagrantVMMemory	- Integer - Default: 6144 - The amount of RAM assigned to the VM in MB.\n" \
			+ "* proxyServer		- String - Default: none - The proxy server value passed to vagrant.\n"

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('deployAllFromNexus'),
				DevtoolUtils.getPluginTaskName('openshiftUp'),
				DevtoolUtils.getPluginTaskName('dockerStart'),
				DevtoolUtils.getPluginTaskName('vagrantUp'),
				DevtoolUtils.getPluginTaskName('vagrantInstallPlugins')
			], group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('up')) {}
	}

	private Task upMaxPower(Project project) {
		String description = 'Does a vagrantReload and starts openshift with higher VM power settings. Sets vagrant RAM=10G and CPU=6.'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('initVagrantMaxPower'),
				DevtoolUtils.getPluginTaskName('up')
			],
			group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('upMaxPower')) {
		}
	}

	private Task reload(Project project) {
		String description = 'Does a vagrantReload and starts openshift with openshiftUp. Does the same as restart.'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('openshiftPortForwardAll'),
				DevtoolUtils.getPluginTaskName('openshiftUp'),
				DevtoolUtils.getPluginTaskName('dockerStart'),
				DevtoolUtils.getPluginTaskName('vagrantReload')
			],
			group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('reload')) {}
	}


	private Task reloadMaxPower(Project project) {
		String description = 'Does a vagrantReload and starts openshift with higher VM power settings. Sets vagrant RAM=10G and CPU=6.'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('initVagrantMaxPower'),
				DevtoolUtils.getPluginTaskName('reload')
			],
			group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('reloadMaxPower')) {
			
		}
	}

	private Task recreate(Project project) {
		String description = 'Destroy and up the local development VM with default vagrant settings for VM such as RAM and CPU. Because of openshiftPortForwardAll task, this task can only be run from the host.'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('destroy'),
				DevtoolUtils.getPluginTaskName('up')
			],
			group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('recreate')) {
		}
	}

	private Task recreateMaxPower(Project project) {
		String description = 'Destroy and up the local development VM with higher vagrant settings for VM: RAM=10G and CPU=6. Because of openshiftPortForwardAll task, this task can only be run from the host.'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('initVagrantMaxPower'),
				DevtoolUtils.getPluginTaskName('recreate')
			],
			group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('recreateMaxPower')) {
		}
	}

	private Task destroy(Project project) {
		String description = 'Destroy the local development VM'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('vagrantDestroy')
			],
			group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('destroy')) {
		}
	}

	private Task bashSetup(Project project) {
		String description = 'Setup bash profile file with proxy functions pon and pof. On linux will use Squid proxy 10.53.99.81:3128. On windows will use cmcc_px_prod.dkd1.root4.net that requires username and password.'

		project.task([group: ['Useful'], description: description], DevtoolUtils.getPluginTaskName('bashSetup')) {
			doFirst {
				String username
				String pass
				String proxy
				if (SystemUtils.IS_OS_WINDOWS) {
					project.exec {
						workingDir "${project.projectDir}/provision"
						username = System.getenv("USERNAME") ?: console.readLine('> Please enter your network username: ').toString()
						pass = System.getenv("PASSWORD") ?: console.readPassword('> Please enter your network password: ').toString()
						proxy = "$username:$pass@${project.devtool.winProxyServer}"

						commandLine "bash", "-c", "source initialise.sh  ${proxy} ${project.devtool.noProxy}"
					}
				} else {
					project.exec {
						workingDir "${project.projectDir}/provision"

						commandLine "bash", "-c", "source initialise.sh  ${project.devtool.proxyServer} ${project.devtool.noProxy}"
					}
				}
			}
		}
	}
}
