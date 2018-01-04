package com.mtbvang.plugin

import com.hubspot.jinjava.Jinjava
import com.mtbvang.DevtoolFileUtils
import com.mtbvang.DevtoolUtils
import com.mtbvang.Tests
import com.mtbvang.tasks.Deploy
import com.mtbvang.tasks.Devtool
import com.mtbvang.tasks.Docker
import com.mtbvang.tasks.Openshift
import com.mtbvang.tasks.Vagrant

import java.nio.file.Path

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.yaml.snakeyaml.Yaml
import org.slf4j.*

//@Slf4j
class DevtoolPlugin implements Plugin<Project> {

	Logger log = LoggerFactory.getLogger(DevtoolPlugin.class)

	private def extension

	void apply(Project project) {
		//project.setDefaultTasks(['up'])
		Map config = getConfig(project)
		extension = dynamicallyConstructExtension(project, config, extension)
		setProxy(project, config)

		addResourcesToRootProject(project)

		// Create all tasks before specifying dependencies to avoid null pointers.
		Docker docker = new Docker(project)
		Openshift openshift = new Openshift(project)
		Vagrant vagrant = new Vagrant(project)
		Deploy deploy = new Deploy(project)
		Tests tests = new Tests(project, extension)
		Devtool devtool = new Devtool(project)

		// Task dependencies for devtool level dependencies. All other dependencies go with their respective classes.

		// Ordering for vagrantReloadMaxPower
		//		project.getTaskByPluginName('vagrantReload').mustRunAfter(project.getTaskByPluginName("initVagrantMaxPower"))
		//		// Ordering for vagrantRecreate
		//		project.getTaskByPluginName('vagrantUp').mustRunAfter(project.getTaskByPluginName('vagrantDestroy'))
		//		// Ordering for vagrantRecreateMaxPower
		//		project.getTaskByPluginName('vagrantRecreateMaxPower').mustRunAfter(project.getTaskByPluginName('initVagrantMaxPower'))
		//
		//		// Ordering for Devtool.upMaxPower
		//		project.getTaskByPluginName('up').mustRunAfter(project.getTaskByPluginName('initVagrantMaxPower'))
		//		// Ordering for Devtool.up task
		//		project.getTaskByPluginName('deployAllFromNexus').mustRunAfter(project.getTaskByPluginName('openshiftUp'))
		//		project.getTaskByPluginName('openshiftUp').mustRunAfter(project.getTaskByPluginName('dockerStart'))
		//		project.getTaskByPluginName('dockerStart').mustRunAfter(project.getTaskByPluginName('vagrantUp'))
		//		project.getTaskByPluginName('vagrantUp').mustRunAfter(project.getTaskByPluginName('vagrantInstallPlugins'))
		//		// Ordering for Devtool.reload
		//		project.getTaskByPluginName('openshiftPortForwardAll').mustRunAfter(project.getTaskByPluginName('openshiftUp'))
		//		project.getTaskByPluginName('openshiftUp').mustRunAfter(project.getTaskByPluginName('dockerStart'))
		//		project.getTaskByPluginName('dockerStart').mustRunAfter(project.getTaskByPluginName('vagrantReload'))
		//		// Ordering for Devtool.recreate
		//		project.getTaskByPluginName('up').mustRunAfter(project.getTaskByPluginName('destroy'))
		//		// Ordering for Devtool.recreateMaxPower
		//		project.getTaskByPluginName('recreate').mustRunAfter(project.getTaskByPluginName('initVagrantMaxPower'))
		//		// Ordering for Evtoo.reloadMaxPower
		//		project.getTaskByPluginName('reload').mustRunAfter(project.getTaskByPluginName('initVagrantMaxPower'))

	}
	
	private def setProxy(Project project, Map config) {
		def envVarHttpProxy = "$System.env.HTTP_PROXY"
		def envVarHttpsProxy = "$System.env.HTTPS_PROXY"
		def envVarNoProxy = "$System.env.NO_PROXY"
		
		println("envVarHttpProxy: ${envVarHttpProxy}")
		
		if(!project.devtool.httpProxy) {
			project.devtool.httpProxy = envVarHttpProxy
		}
		if(!project.devtool.httpsProxy) {
			project.devtool.httpsProxy = envVarHttpsProxy
		}
		if(!project.devtool.noProxy) {
			project.devtool.noProxy = envVarNoProxy
		}
		println("project.devtool.noProxy: ${project.devtool.noProxy}")
	}

	private def addResourcesToRootProject(Project project) {
		log.info("Copying devtool plugin resources to root project dir: ${project.projectDir}")

		if(!(new File(project.projectDir, 'ansible').exists())) {
			DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/ansible"), project.projectDir)
		}
		if(!(new File(project.projectDir, 'provision').exists())) {
			DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/provision"), project.projectDir)
		}
		if(!(new File(project.projectDir, 'Vagrantfile').exists())) {
			DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/Vagrantfile"), project.projectDir)
		}
		
	}

	private def dynamicallyConstructExtension(Project project, Map config, def extension) {
		addMetaClassMethods(project)

		extension = project.extensions.create("devtool", DevtoolPluginExtension)

		// Dynamically add properties to the extension object from the configs.
		config.toSorted({ a, b -> a.key <=> b.key }).each { property ->
			log.info("Creating dynamic extension property: {}, value: {}, type: {}", property.key.camelCase(), property.value, property.value.getClass())
			// Not all properties are set from the project parameters or the config files.
			if(property.key.camelCase().equals("projectDir")) {
				extension.metaClass."${property.key.camelCase()}" = project.projectDir.absolutePath
				assert Objects.equals(project.devtool.projectDir.toString(), project.projectDir.absolutePath)
			}else {
				// Set all metaClass properties to project parameters or the value specified in vars.yml
				log.info("Setting {} to value: {}", property.key.camelCase(), project.hasProperty("${property.key.camelCase()}") ? project.property("${property.key.camelCase()}") : property.value)
				extension.metaClass."${property.key.camelCase()}" = project.hasProperty("${property.key.camelCase()}") ? project.property("${property.key.camelCase()}") : property.value
				log.info("extension {} set to: {}",property.key.camelCase(), extension."${property.key.camelCase()}")
				assert extension."${property.key.camelCase()}" == (project.hasProperty("${property.key.camelCase()}") ? project.property("${property.key.camelCase()}") : property.value)
			}

		}

		extension.metaClass.properties.toSorted({ a, b -> a.name <=> b.name }).each { property ->
			log.debug("devtool extension key: {}, value: {}, type: {} ", property.name, project.devtool."${property.name}", extension."${property.name}".getClass())
		}

		extension.metaClass.projectParentDir = project.projectDir.getParent()
		assert Objects.equals(project.devtool.projectParentDir.toString(), project.projectDir.getParent())
		log.debug("project.devtool.projectParentDir: ${project.devtool.projectParentDir}")

		extension.metaClass.guestProjectDir = "/vagrant/${project.name}"
		assert Objects.equals(project.devtool.guestProjectDir.toString(), "/vagrant/${project.name}".toString())
		log.debug("project.devtool.guestProjectDir: ${project.devtool.guestProjectDir}")

		extension
	}

	private Map getConfig(Project project) {
		Yaml ymlParser = new Yaml()

		Map vars = ymlParser.load(("${project.projectDir}/vars.yml" as File).text)
		String renderedVars = new Jinjava().render(("${project.projectDir}/vars.yml" as File).getText("UTF-8"), vars)
		vars = ymlParser.load(renderedVars)

		String renderedProjects = new Jinjava().render(("${project.projectDir}/apps.yml" as File).getText("UTF-8"), vars)
		Map mappedConfig = ymlParser.load(renderedProjects)
		mappedConfig += vars

		mappedConfig
	}

	private void addMetaClassMethods(Project project) {
		/*
		 * Converts yaml property names to camelCase. Works with hypens (-) and underscores (_)
		 */
		String.metaClass.camelCase = {
			delegate.replaceAll(/[-|_](.)/, { match -> match[1].toUpperCase() })
		}

		/**
		 * Gets the Task object of taskName without having to think about the task prefix.
		 */
		Project.metaClass.getTaskByPluginName = { taskName ->
			project.tasks.findByName(DevtoolUtils.getPluginTaskName(taskName))

		}

	}

}
