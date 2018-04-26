package com.mtbvang

import static org.gradle.testkit.runner.TaskOutcome.*

import java.nio.file.*
import java.nio.file.attribute.*

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Before
import org.slf4j.*
import org.yaml.snakeyaml.Yaml

import com.hubspot.jinjava.Jinjava

/*
 * Functional tests written using GradleRunner. We're creating a test folder and copying 
 * over the files used in the root project. Refer to  copyTestFiles() for a list of files.
 * 
 * The setup method runs before every test while the cleanUp task only runs at he test run to 
 * allow reuse of the VM. The tests themselves should determine the VMs state by calling
 * ensureVMRunning() and ensureVMProvisioned() as needed.
 */
class FunctionalTest {
	private static Logger log = LoggerFactory.getLogger(FunctionalTest.class)
	// FIXME Maybe revert back to using TemporaryFolder to get auto clean up instead of manual cleanUp()
	//@ClassRule public static final TemporaryFolder testProjectDir = new TemporaryFolder();

	// Comment these two lines and uncomment the two commented lines to run tests with an existing tmp diretory and VM. Replace the /tmp/devtoolxxxx with the path of an existing test folder if you need.
	public static Path testBaseDir
	public static Path testDir
	public static Path testFabric8Dir
	
	static boolean isVMProvisioned = false
//		public static Path testProjectDir = new File('/tmp/devtool3781720399035113494').toPath()
//		static boolean isVMProvisioned = true

	protected static def vagrantOpenshiftHostForwardPort
	static BuildResult result
	static def vagrantVMCPUs = 1
	static def vagrantVMMemory = 1024

	static def ant = new AntBuilder()
	
	@BeforeClass
	static void setupOnlyOnce() {
		println("onlyOnce setup called.")
		testBaseDir = Files.createTempDirectory('devtool')
		testDir = testBaseDir.resolve('devtool')
		testDir.toFile().mkdir()
		testFabric8Dir = testBaseDir.resolve('fabric8')
		testFabric8Dir.toFile().mkdir()
		copyTestFiles()
		vagrantOpenshiftHostForwardPort = TestUtils.getRandomPort(9999, 8999)
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
	void ensureVMProvisioned(memory = 6144, cpu = 1, gui = false) {

		if(!isVMProvisioned) {
			vagrantVMMemory = memory
			vagrantVMCPUs = cpu
			runVagrantTask('vagrantRecreate', '', gui)
			println("Provisioning VM. This might take over 10 minutes...")
			//			runVagrantTask('vagrantProvision', '')
			isVMProvisioned = true
		}
	}

	@AfterClass
	static void cleanUpOnlyOnce() {

		isVMProvisioned = false
		result = null

		println("Cleaning up after tests. Deleting " + testBaseDir.toFile())
		TestUtils.vagrantCleanUp(testBaseDir.toFile())
		testBaseDir = null
	}

	BuildResult runVagrantTask(String taskName) {

		runVagrantTask(taskName, false, "")

	}

	BuildResult runVagrantTask(String taskName, String vagrantProvisionOpts, Boolean gui = false) {

		runVagrantTask(taskName, gui, vagrantProvisionOpts)

	}

	BuildResult runVagrantMaxPowerTask(String taskName, String vagrantProvisionOpts) {

		runVagrantMaxPowerTask(taskName, false, vagrantProvisionOpts)

	}

	BuildResult runVagrantForceProvisioning(String taskName) {
		runVagrantTask(taskName, false, '--provision')
	}

	BuildResult runVagrantTask(String taskName, boolean vagrantGui, String vagrantProvisionOpts) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testDir.toFile())
				.withArguments(taskName,
				"-PvagrantGui=${vagrantGui}",
				"-PvagrantProvisionOpts=${vagrantProvisionOpts}",
				"-PvagrantOpenshiftHostForwardPort=${vagrantOpenshiftHostForwardPort}",
				"-PvirtualboxVMName=${testBaseDir.toFile().getName()}",
				"-PvagrantVMCPUs=${vagrantVMCPUs}",
				"-PvagrantVMMemory=${vagrantVMMemory}")
				.withPluginClasspath()
				.forwardOutput()
				.build()
	}

	BuildResult runVagrantMaxPowerTask(String taskName, boolean vagrantGui, String vagrantProvisionOpts) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testDir.toFile())
				.withArguments(taskName,
				"-PvagrantGui=${vagrantGui}",
				"-PvagrantProvisionOpts=${vagrantProvisionOpts}",
				"-PvagrantOpenshiftHostForwardPort=${vagrantOpenshiftHostForwardPort}",
				"-PvirtualboxVMName=${testBaseDir.toFile().getName()}")
				.withPluginClasspath()
				.forwardOutput().withDebug(true)
				.build()
	}

	BuildResult runTask(String taskName) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testDir.toFile())
				.withArguments(taskName)
				.withPluginClasspath()
				.forwardOutput()
				.build()
	}

	BuildResult runTaskExpectingFail(String taskName) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testDir.toFile())
				.withArguments(taskName)
				.withPluginClasspath()
				.forwardOutput()
				.buildAndFail()
	}

	static void copyTestFiles() {

		if(Files.exists(Paths.get(testDir.toString(), 'Vagrantfile'))) {
			println("Vagrantfile exists. Not copying over files for testing.")
			return
		}

		println("Copying over resource files for testing to: ${testDir}")
		
		def proc = "git clone https://github.com/mtbvang/fabric8-devops ${testFabric8Dir.toString()}/fabric8-devops".execute()
		def output = new StringBuffer()
		proc.consumeProcessOutputStream(output)
		println proc.text
		println output.toString()

		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/vars.yml"), testDir.toFile())
		DevtoolFileUtils.copyResourcesRecursively(super.getClass().getResource("/devtool/build.gradle"), testDir.toFile())
	}
}
