package com.mtbvang

import com.hubspot.jinjava.Jinjava
import com.mtbvang.DevtoolFileUtils

import java.nio.file.Path
import java.util.Map

import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.yaml.snakeyaml.Yaml
import org.slf4j.*
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*

import org.junit.Before
import org.junit.BeforeClass
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import java.nio.file.*

import java.nio.file.attribute.*

class FunctionalTest {
	private static Logger log = LoggerFactory.getLogger(FunctionalTest.class)
	// FIXME revert back to using TemporaryFolder to get auto clean up
	//@ClassRule public static final TemporaryFolder testProjectDir = new TemporaryFolder();
	public final Path testProjectDir = Files.createTempDirectory('devtool')
	//public final Path testProjectDir = new File('/tmp/devtoolTesting').toPath()
	
	@Before
	void setup() {
		println("Using testProjectDir: ${testProjectDir}")

		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/apps.yml"), testProjectDir.toFile());
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/vars.yml"), testProjectDir.toFile());
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/build.gradle"), testProjectDir.toFile());
	}

	Map getConfig() {
		Yaml ymlParser = new Yaml()

		Map vars = ymlParser.load(("${testProjectDir}/ansible/vars.yml" as File).text)
		String renderedVars = new Jinjava().render(("${testProjectDir}/ansible/vars.yml" as File).getText("UTF-8"), vars)
		vars = ymlParser.load(renderedVars)

		String renderedProjects = new Jinjava().render(("${testProjectDir}/ansible/apps.yml" as File).getText("UTF-8"), vars)
		Map mappedConfig = ymlParser.load(renderedProjects)
		mappedConfig += vars

		mappedConfig
	}

	BuildResult runTask(String taskName, String vagrantProvisionOpts) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(taskName,
				'-PvagrantGui=false',
				'-PvagrantTesting=true',
				"-PvagrantProvisionOpts=${vagrantProvisionOpts}",
				"-PvirtualboxVMName=${testProjectDir.toFile().getName()}")
				.withPluginClasspath()
				.withDebug(true)
				.build()
	}
}
