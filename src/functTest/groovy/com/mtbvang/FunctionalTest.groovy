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
 * setup method runs before every test while the cleanUp task only runs at he test run to 
 * allow reuse of the VM. The tests themselves should determine the VMs state by calling
 * ensureVMRunning() and ensureVMProvisioned() as needed.
 */
class FunctionalTest {
	private static Logger log = LoggerFactory.getLogger(FunctionalTest.class)
	// FIXME Maybe revert back to using TemporaryFolder to get auto clean up instead of manual cleanUp()
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

		vagrantVMCPUs = 1
		vagrantVMMemory = 1024

		println("isVMProvisioned: " + isVMProvisioned)

		if(!testProjectDir) {
			testProjectDir = Files.createTempDirectory('devtool')
			println("Creating test dir: " + testProjectDir)
			copyTestFiles()
			vagrantOpenshiftHostForwardPort = TestUtils.getRandomPort(9999, 8999)
		}
	}

	/*
	 * Startup up an unprovisioned VM
	 */
	void ensureVMRunning() {
		println("Ensuring VM is running as test fixture for all tests...")
		runVagrantTask('vagrantUp', '--no-provision')
	}

	/*
	 * Provision the VM for test that require a provisioned VM e.g. the openshift tests.
	 */
	void ensureVMProvisioned() {

		if(!isVMProvisioned) {
			vagrantVMMemory = 6144
			runVagrantTask('vagrantRecreate', '')
			println("Provisioning VM. This might take over 10 minutes...")
			runVagrantTask('vagrantProvision', '')
			isVMProvisioned = true
		}
	}

	@AfterClass
	static void cleanUp() {

		isVMProvisioned = false
		result = null
		
		println("Cleaning up after tests. Deleting " + testProjectDir.toFile())
		TestUtils.vagrantCleanUp(testProjectDir.toFile())
		testProjectDir = null
	}

	BuildResult runVagrantTask(String taskName) {

		runVagrantTask(taskName, false, "")

	}

	BuildResult runVagrantTask(String taskName, String vagrantProvisionOpts) {

		runVagrantTask(taskName, false, vagrantProvisionOpts)

	}

	BuildResult runVagrantMaxPowerTask(String taskName, String vagrantProvisionOpts) {

		runVagrantMaxPowerTask(taskName, false, vagrantProvisionOpts)

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

	BuildResult runVagrantMaxPowerTask(String taskName, boolean vagrantGui, String vagrantProvisionOpts) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(taskName,
				"-PvagrantGui=${vagrantGui}",
				"-PvagrantProvisionOpts=${vagrantProvisionOpts}",
				"-PvagrantOpenshiftHostForwardPort=${vagrantOpenshiftHostForwardPort}",
				"-PvirtualboxVMName=${testProjectDir.toFile().getName()}")
				.withPluginClasspath()
				.forwardOutput().withDebug(true)
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

		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/vars.yml"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/build.gradle"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/Vagrantfile"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/tests"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/testSetup.sh"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/provisionTestVM.sh"), testProjectDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/provision"), testProjectDir.toFile())
	}
}
