package com.mtbvang

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import static org.junit.Assert.*;
import org.slf4j.*

import java.util.stream.Collectors

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.*

/*
 * We test that the vagrant commands are working correctly without ansible provisioning. We only do a dry run
 * of the ansible playbook. This speeds things up and provisioning testing is not necessary because that is 
 * tested in the individual ansible roles.
 * 
 * @author Vang Nguyen
 *
 */
class OpenshiftTest extends FunctionalTest {

	private static Logger log = LoggerFactory.getLogger(OpenshiftTest.class)


	//@Test
	/*
	 * Testing vagrant77// up with all provisioners except ansible
	 */
	void openshiftUp() {
		try {

			//def openshiftPortHost = TestUtils.getRandomPort(9999, 8999)
			def openshiftPortHost = 9149
			println("openshiftUpTest run using openshiftPortHost: ${openshiftPortHost}")

			BuildResult result

			result = runTaskWithNoProvisioning('vagrantUp', openshiftPortHost)
			println("vagrantUp run in openshiftUpTest output: " + result.output)
			result.task(':vagrantUp').outcome == SUCCESS

			result = runTaskWithNoProvisioning('openshiftUp', openshiftPortHost)
			println("openshiftUp run output: " + result.output)
			result.task(':openshiftUp').outcome == SUCCESS
		} finally {
			//TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	//@Test
	void openshiftStatus() {
		try {

			def openshiftPortHost = TestUtils.getRandomPort(9999, 8999)
			println("openshiftStatus test run using openshiftPortHost: ${openshiftPortHost}")

			BuildResult result

			result = runTaskWithNoProvisioning('openshiftStatus', openshiftPortHost)
			println("openshiftStatus test run output: " + result.output)
			result.task(':openshiftStatus').outcome == SUCCESS
			assertThat(result.output).contains('VM must be created before running this command')
		} finally {
			//TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	//@Test
	void openshiftPortForwardApp() {
		7
		try {
			BuildResult result

			result = runTaskWithNoProvisioning('openshiftPortForwardZuulProxy', TestUtils.getRandomPort(9999, 8999))
			println("openshiftPortForwardApp test run output: " + result.output)
			result.task(':openshiftPortForwardApp').outcome == SUCCESS
			//assertThat(result.output).contains('VM must be created before running this command')
		} finally {
			//TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	BuildResult runTaskWithProvisioning(String taskName, int openshiftPortHost) {
		runTask(taskName, true, false, '--provision', "${testProjectDir.toFile().getName()}", openshiftPortHost)
	}

	BuildResult runTaskWithNoProvisioning(String taskName, int openshiftPortHost) {
		runTask(taskName, true, false, '--no-provision', "${testProjectDir.toFile().getName()}", openshiftPortHost)
	}

	BuildResult runTask(String taskName, boolean vagrantGui, boolean vagrantTesting, String vagrantProvisionOpts, String virtualboxVMName, int openshiftPortHost) {
		BuildResult result = GradleRunner.create()
				.withProjectDir(testProjectDir.toFile())
				.withArguments(taskName,
				"-PvagrantGui=${vagrantGui}",
				"-PvagrantTesting=${vagrantTesting}",
				"-PvagrantProvisionOpts=${vagrantProvisionOpts}",
				"-PvirtualboxVMName=${virtualboxVMName}",
				"-PopenshiftPortHost=${openshiftPortHost}")
				.withPluginClasspath()
				.build()
	}
}
