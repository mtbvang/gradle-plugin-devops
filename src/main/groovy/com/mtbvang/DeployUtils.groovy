package com.mtbvang

import org.gradle.api.Task
import org.slf4j.*
import org.gradle.api.Project
import org.apache.commons.lang3.SystemUtils

public final class DeployUtils {

	private static Logger log = LoggerFactory.getLogger(DeployUtils.class)

	static runLocalDevPlaybookViaVagrant = { project ->
		def description = 'Run ansible/local-development.yml playbook from host by sshing into Guest.'
		
		def command = "ANSIBLE_STDOUT_CALLBACK=debug ansible-playbook " \
				+ "/vagrant/${project.name}/ansible/deploy.yml --tags=${project.devtool.ansibleTags} ${project.devtool.ansibleVerbosity} "
		
		project.devtool.each { property ->
			command += "--extra-vars=${property.key}=${property.value} "	
		}
		
		log.info("Ansible playbook command: {}", command)
				
		project.exec { commandLine "bash", "-c", "vagrant ssh ${project.devtool.vagrantVMName} -c '${command}'" }
	}

}
