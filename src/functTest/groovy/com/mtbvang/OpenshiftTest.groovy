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


	@Test
	/*
	 * Testing vagrant77// up with all provisioners except ansible
	 */
	void openshiftUp() {
		try {

			def vagrantOpenshiftHostForwardPort = TestUtils.getRandomPort(9999, 8999)
			//def vagrantOpenshiftHostForwardPort = 9149
			println("openshiftUpTest run using vagrantOpenshiftHostForwardPort: ${vagrantOpenshiftHostForwardPort}")

			BuildResult result

			result = runVagrantTaskWithProvisioning('vagrantUp', vagrantOpenshiftHostForwardPort)
			println("vagrantUp run in openshiftUpTest output: " + result.output)
			result.task(':vagrantUp').outcome == SUCCESS

			result = runOpenshiftTask('openshiftUp', vagrantOpenshiftHostForwardPort)
			println("openshiftUp run output: " + result.output)
			result.task(':openshiftUp').outcome == SUCCESS
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	//@Test
	void openshiftStatus() {
		try {

			def vagrantOpenshiftHostForwardPort = TestUtils.getRandomPort(9999, 8999)
			println("openshiftStatus test run using vagrantOpenshiftHostForwardPort: ${vagrantOpenshiftHostForwardPort}")

			BuildResult result

			result = runTaskWithNoProvisioning('openshiftStatus', vagrantOpenshiftHostForwardPort)
			println("openshiftStatus test run output: " + result.output)
			result.task(':openshiftStatus').outcome == SUCCESS
			assertThat(result.output).contains('VM must be created before running this command')
		} finally {
			//TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	//@Test
	void openshiftPortForwardApp() {
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
	
}
