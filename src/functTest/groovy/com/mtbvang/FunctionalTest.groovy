package com.mtbvang

import static org.gradle.testkit.runner.TaskOutcome.*

import java.nio.file.*
import java.nio.file.attribute.*

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.AfterClass
import org.junit.Before
import org.slf4j.*
import org.yaml.snakeyaml.Yaml

import com.hubspot.jinjava.Jinjava

/*
 * Tests tasks using the 
 */
class FunctionalTest {
	private static Logger log = LoggerFactory.getLogger(FunctionalTest.class)
	// FIXME revert back to using TemporaryFolder to get auto clean up
	//@ClassRule public static final TemporaryFolder testProjectDir = new TemporaryFolder();
	public static Path testProjectDir
	//public static final Path testProjectDir = new File('/tmp/devtool2450998320394310883').toPath()

	protected static def vagrantOpenshiftHostForwardPort

	static BuildResult result

	static boolean isVMProvisioned = false;

	static def vagrantVMCPUs
	static def vagrantVMMemory


	@Before
	void setup() {

		println("Test setup called.")
		
		vagrantVMCPUs = 2
		vagrantVMMemory = 6144
		
		println("isVMProvisioned: " + isVMProvisioned)

		if(!testProjectDir) {
			testProjectDir = Files.createTempDirectory('devtool')
			println("Creating test dir: " + testProjectDir)
			copyTestFiles()
			vagrantOpenshiftHostForwardPort = TestUtils.getRandomPort(9999, 8999)
		}

		// The test runner currently throws an error because of mustRunAfter constraints. Can run tests on running VM if we want the contraints.
		ensureVMRunning()
	}

	void ensureVMRunning() {
		println("Ensuring VM is running as test fixture for all tests...")
		def sout = new StringBuilder(), serr = new StringBuilder()
		"chmod a+x testSetup.sh".execute(null, testProjectDir.toFile())
		Process proc = "./testSetup.sh ${testProjectDir.toFile().getName()} ${vagrantVMMemory} false ${vagrantVMCPUs} 100 ${vagrantOpenshiftHostForwardPort}".execute(null, testProjectDir.toFile())
		proc.consumeProcessOutput(sout, serr)
		proc.waitFor()
		println "out> $sout err> $serr"
	}

	void ensureVMIsProvisioned() {
		if(!isVMProvisioned) {
			println("Provisioning VM. This might take over 10 minutes...")
			def sout = new StringBuilder(), serr = new StringBuilder()
			"chmod a+x provisionTestVM.sh".execute(null, testProjectDir.toFile())
			Process proc = "./provisionTestVM.sh".execute(null, testProjectDir.toFile())
			proc.consumeProcessOutput(sout, serr)
			proc.waitFor()
			println "out> $sout err> $serr"
			isVMProvisioned = true
		}
	}

	@AfterClass
	static void cleanUp() {

		isVMProvisioned = false
		result = null

		println("Cleaning up after tests. Deleting " + testProjectDir.toFile())
		TestUtils.vagrantCleanUp(testProjectDir.toFile())

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

	BuildResult runVagrantTask(String taskName) {

		runVagrantTask(taskName, false, "")

	}

	BuildResult runVagrantTask(String taskName, String vagrantProvisionOpts) {

		runVagrantTask(taskName, false, vagrantProvisionOpts)

	}

	BuildResult runVagrantForceProvisioning(String taskName) {
		runVagrantTask(taskName, false, '--provision')
	}

	BuildResult runVagrantTask(String taskName, boolean vagrantGui, String vagrantProvisionOpts) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(taskName,
				"-PvagrantGui=${vagrantGui}",
				"-PvagrantProvisionOpts=${vagrantProvisionOpts}",
				"-PvagrantOpenshiftHostForwardPort=${vagrantOpenshiftHostForwardPort}",
				"-PvirtualboxVMName=${testProjectDir.toFile().getName()}",
				"-PvagrantVMCPUs=${vagrantVMCPUs}",
				"-PvagrantVMMemory=${vagrantVMMemory}")
				.withPluginClasspath()
				.forwardOutput()
				.build()
	}

	BuildResult runTask(String taskName) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(taskName)
				.withPluginClasspath()
				.forwardOutput()
				.build()
	}

	BuildResult runTaskExpectingFail(String taskName) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(taskName)
				.withPluginClasspath()
				.forwardOutput()
				.buildAndFail()
	}

	void copyTestFiles() {

		if(Files.exists(Paths.get(testProjectDir.toString(), 'Vagrantfile'))) {
			println("Vagrantfile exists. Not copying over files for testing.")
			return
		}

		println("Copying over resource files for testing to: ${testProjectDir}")

		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/apps.yml"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/vars.yml"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/build.gradle"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/Vagrantfile"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/tests"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/testSetup.sh"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/provisionTestVM.sh"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/ansible"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/provision"), testProjectDir.toFile())
	}
}
