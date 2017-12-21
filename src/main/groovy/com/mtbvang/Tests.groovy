package com.mtbvang

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer

class Tests {

	
	public Tests(Project project, def extension) {
		super();
		
		debug(project, extension)
	}

	private Task debug(Project project, def extension) {
		project.task(DevtoolUtils.getPluginTaskName('debug')) {
			description 'Debugging task'
			group = "Useful"

			doLast {
				println "projectDir = ${extension.projectDir}"
			}
		}
	}
}
