/**
 * 
 */
package com.mtbvang.tasks

import com.hubspot.jinjava.Jinjava
import com.mtbvang.DevtoolUtils

import java.util.Map

import org.gradle.api.Project
import org.gradle.api.Task
import org.slf4j.*
import org.yaml.snakeyaml.Yaml
import org.apache.commons.lang3.SystemUtils

/**
 * @author Vang Nguyen
 *
 */
class Vagrant {

	Logger log = LoggerFactory.getLogger(Vagrant.class)

	private def vagrantEnvVars = [:]
	private def vagrantCommandEnvVars = ""

	public Vagrant(Project project) {
		super();
		if (SystemUtils.IS_OS_WINDOWS && !project.devtool.pon) {
			pon = 'source ~/.bash_profile && pon && '
		}

		vagrantEnvVars << ["pon": "${project.devtool.pon}"]
		vagrantEnvVars << ["ANSIBLE_GALAXY": "${project.devtool.runAnsibleGalaxy}"]
		vagrantEnvVars << ["VM_NAME": "${project.devtool.virtualboxVMName}"]
		vagrantEnvVars << ["VM_MEMORY": "${project.devtool.vagrantVMMemory}"]
		vagrantEnvVars << ["VM_GUI": "${project.devtool.vagrantGui}"]
		vagrantEnvVars << ["VM_CPUS": "${project.devtool.vagrantVMCPUs}"]
		vagrantEnvVars << ["VM_CPU_CAP": "${project.devtool.vagrantVMCPUCap}"]
		vagrantEnvVars << ["VB_GUEST": "${project.devtool.vagrantVBGuest}"]
		vagrantEnvVars << ["OPENSHIFT_PORT_HOST": "${project.devtool.vagrantOpenshiftHostForwardPort}"]
		vagrantEnvVars << ["VM_NAME": "${project.devtool.virtualboxVMName}"]
		vagrantEnvVars << ["VAGRANT_TESTING": "${project.devtool.vagrantTesting}"]

		vagrantEnvVars.each{ k, v ->
			if(v) {
				log.info("Adding vagrant environment variable to map: {}: {}", k, v)
				vagrantCommandEnvVars += "${k}=${v} "
			}
		}

		// Create all tasks before specifying dependencies to avoid null pointers.
		initVagrantMaxPower(project)
		vagrantUp(project)
		vagrantHalt(project)
		vagrantStatus(project)
		vagrantInstallPlugins(project)
		vagrantReload(project)
		vagrantReloadMaxPower(project)
		vagrantDestroy(project)
		vagrantRecreate(project)
		vagrantRecreateMaxPower(project)
		vagrantProvision(project)
		vagrantProvisionWithAnsible(project)

	}


	private Task initVagrantMaxPower(Project project) {
		String description = 'Initialises higher VM power settings. Sets vagrant RAM=10G and CPU=6.'

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('initVagrantMaxPower')) {
			doFirst {
				project.devtool.vagrantVMCPUs = 2
				project.devtool.vagrantVMMemory = 10240
				log.info("initVagrantMaxPower vagrantVMCPUs: {}, vagrantVMMemory: {}", project.devtool.vagrantVMCPUs, project.devtool.vagrantVMMemory)
			}
		}
	}

	private Task vagrantUp(Project project) {
		String description = """Create the local development VM. 
	   * Parameter: pon - Default: blank. A command line command to turn on the proxy. 
	   * Parameter: runAnsibleGalaxy - Default: true. True to run ansible galaxy with --force 
	   * Parameter: vagrantVMMemory - HTTP proxy to use. 
	   * Parameter: httpsProxy  - HTTPS proxy to use 
	   * Parameter: vagrantVMMemory - Memory in MB to assing to the VM. 
	   * Parameter: vagrantVMCPUs - Number of CPUs to start up VM with. 
	   * Parameter: noProxy - no_proxy environment variable on VM OS. 
	   * Parameter: runAnsibleGalaxy  - Whether or not vagrant spins up the VM in testing mode. Look at what is being done with the TESTING variable in /Vagrantfile. 
	   * Parameter: vagrantVMName	- The name of the vagrant VM that this command applies to. Set to blank for all VMs. 
	   * Parameter: vagrantVBGuest - Determines with guest additions plugin gets run. 
	   * Parameter: vagrantGui	  - True will bring up a desktop for the VM. 
	   * Parameter: vagrantProvider  - default=virtualbox. The virutal machine provider to bring up the VM.
		"""
		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantUp')) {
			doFirst {
				def gitUsername = System.getenv("GIT_USERNAME") ?: System.console().readLine('What is your git username?:')
				def gitPassword = System.getenv("GIT_PASSWORD") ?: System.console().readPassword('What is your git password?:')
				def vagrantCommand = vagrantCommandEnvVars \
					+ "GIT_USERNAME='${gitUsername}' GIT_PASSWORD=${gitPassword} vagrant up ${project.devtool.vagrantVMName} --provider ${project.devtool.vagrantProvider} ${project.devtool.vagrantProvisionOpts}"

				log.debug("vagrant command: ${vagrantCommand}")

				project.exec {
					ignoreExitValue false
					commandLine "bash", "-c", vagrantCommand
				}
			}
		}
	}

	private Task vagrantDestroy(Project project) {
		String description = 'Destroy the local development VM'

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantDestroy')) {
			doFirst {
				project.exec {
					ignoreExitValue true
					commandLine "bash", "-c", "vagrant destroy -f ${project.devtool.vagrantVMName}"
				}
			}
		}
	}

	private Task vagrantHalt(Project project) {
		String description = 'Halt the local development VM'

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantHalt')) {
			doFirst {
				project.exec { commandLine "bash", "-c", "vagrant halt -f ${project.devtool.vagrantVMName}"}
			}
		}
	}

	private Task vagrantStatus(Project project) {
		String description = 'Status the local development VM'

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantStatus')) {
			doFirst {
				project.exec { commandLine "bash", "-c", "vagrant status"}
			}
		}
	}

	private Task vagrantInstallPlugins(Project project) {
		String description = 'Installs vagrant plugins.'
		project.task([dependsOn: ['bashSetup'], group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantInstallPlugins')) {
			doFirst {
				if (SystemUtils.IS_OS_WINDOWS) {
					project.exec { commandLine "bash", "-c", "source ~/.bash_profile && pon && vagrant plugin install ${project.devtool.vagrantPlugins}" }
				} else {
					project.exec { commandLine "bash", "-c", "vagrant plugin install ${project.devtool.vagrantPlugins}" }
				}
			}
		}
	}

	private Task vagrantUninstallPlugins(Project project) {
		String description = 'Uninstalls vagrant plugins.'
		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantUninstallPlugins')) {
			doFirst {
				project.exec { commandLine "bash", "-c", "vagrant plugin uninstall ${project.devtool.vagrantPlugins}" }
			}
		}
	}

	private Task vagrantReload(Project project) {
		String description = """Halt and restart the local development VM.
	   * Parameter: pon - Default: blank. A command line command to turn on the proxy. 
	   * Parameter: runAnsibleGalaxy - Default: true. True to run ansible galaxy with --force 
	   * Parameter: vagrantVMMemory - HTTP proxy to use. 
	   * Parameter: httpsProxy  - HTTPS proxy to use 
	   * Parameter: vagrantVMMemory - Memory in MB to assing to the VM. 
	   * Parameter: vagrantVMCPUs - Number of CPUs to start up VM with. 
	   * Parameter: noProxy - no_proxy environment variable on VM OS. 
	   * Parameter: runAnsibleGalaxy  - Whether or not vagrant spins up the VM in testing mode. Look at what is being done with the TESTING variable in /Vagrantfile. 
	   * Parameter: vagrantVMName	- The name of the vagrant VM that this command applies to. Set to blank for all VMs. 
	   * Parameter: vagrantVBGuest - Determines with guest additions plugin gets run. 
	   * Parameter: vagrantGui	  - True will bring up a desktop for the VM. 
	   * Parameter: vagrantProvider  - default=virtualbox. The virutal machine provider to bring up the VM.
		"""

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantReload')) {
			doFirst {
				def vagrantCommand = vagrantCommandEnvVars \
					+ "vagrant reload ${project.devtool.vagrantVMName} ${project.devtool.vagrantProvisionOpts}"

				println("vagrant command: ${vagrantCommand}")

				project.exec {
					ignoreExitValue true
					commandLine "bash", "-c", vagrantCommand
				}
			}
		}
	}

	private Task vagrantReloadMaxPower(Project project) {
		String description = 'Does a vagrantReload and starts openshift with higher VM power settings. Sets vagrant RAM=10G and CPU=6.'

		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('initVagrantMaxPower'),
				DevtoolUtils.getPluginTaskName('vagrantReload')
			], group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantReloadMaxPower')) {
		}
	}

	private Task vagrantRecreate(Project project) {
		String description = 'Destroy and up the local development VM with default vagrant settings for VM such as RAM and CPU. Because of openshiftPortForwardAll task, this task can only be run from the host.'
		
		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('vagrantDestroy'),
				DevtoolUtils.getPluginTaskName('vagrantUp')
			],
			group: ['Vagrant'],
			description: description], DevtoolUtils.getPluginTaskName('vagrantRecreate')) {
		}
	}

	private Task vagrantRecreateMaxPower(Project project) {
		String description = 'Destroy and up the local development VM with default vagrant settings for VM such as RAM and CPU. Because of openshiftPortForwardAll task, this task can only be run from the host.'
		
		project.task([dependsOn: [
				DevtoolUtils.getPluginTaskName('initVagrantMaxPower'),
				DevtoolUtils.getPluginTaskName('vagrantRecreate')
			],
			group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantRecreateMaxPower')) {
		}
	}

	private Task vagrantProvision(Project project) {
		String description = 'Provision the local development VM'

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantProvision')) {
			doLast {

				def vagrantCommand = vagrantCommandEnvVars \
				+ "vagrant provision ${project.devtool.vagrantVMName} ${project.devtool.vagrantProvisionOpts}"

				println("vagrant command: ${vagrantCommand}")

				project.exec {
					ignoreExitValue true
					commandLine "bash", "-c", vagrantCommand
				}
			}
		}
	}

	private Task vagrantProvisionWithAnsible(Project project) {
		String description = 'Provision the local development VM'

		project.task([group: ['Vagrant'], description: description], DevtoolUtils.getPluginTaskName('vagrantProvisionWithAnsible')) {
			doLast {
				project.exec {
					commandLine "bash", "-c", "ANSIBLE_GALAXY=true vagrant provision ${project.devtool.vagrantVMName} --provision-with=ansibleGalaxy,ansiblePlaybook"
				}
			}
		}
	}
	
}
