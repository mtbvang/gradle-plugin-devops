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
class VagrantTest extends FunctionalTest {

	private static Logger log = LoggerFactory.getLogger(VagrantTest.class)

	
	@Test
	/*
	 * Testing vagrant up with all provisioners except ansible
	 */
	void vagrantUp() {
		try{
			BuildResult result = super.runVagrantTask('vagrantUp', '--provision-with bootstrap,bootstrapCentos,ansibleGalaxy')
			println("vagrantUp run output: " + result.output)
			assertThat(result.output).contains("Bringing machine 'centos' up with 'virtualbox' provider")
			assertThat(result.output).contains('Machine booted and ready!')
			assertThat(result.output).contains('Running provisioner: bootstrap (shell)...')
			assertThat(result.output).contains('Running provisioner: bootstrapCentos (shell)...')
			assertThat(result.output.replaceAll("\\s+","")).contains('Removed:centos:PackageKit.x86_640:1.1.5-1.el7.centos'.replaceAll("\\s+",""))
			assertThat(result.output.replaceAll("\\s+","")).contains('Installed:centos:ansible.noarch'.replaceAll("\\s+",""))
			assertThat(result.output.replaceAll("\\s+","")).doesNotContain('Vagrant environment or target machine is required to run this command'.replaceAll("\\s+",""))
			assertThat(result.output.replaceAll("\\s+","")).doesNotContain('Vagrant cannot forward the specified ports on this VM'.replaceAll("\\s+",""))
			result.task(':vagrantUp').outcome == SUCCESS
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	@Test
	void vagrantReload() {
		try {
			BuildResult result = super.runVagrantTask('vagrantReload', '--no-provision')
			println("vagrantReload run output: " + result.output)
			assertThat(result.output).contains('VM not created. Moving on')
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	@Test
	void vagrantReloadMaxPower() {
		try {
			BuildResult result = super.runVagrantTask('vagrantReloadMaxPower', '--no-provision')
			println("vagrantReloadMaxPower run output: " + result.output)
			assertThat(result.output).contains('VM not created. Moving on')
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	@Test
	void vagrantHalt() {
		try {
			BuildResult result = super.runVagrantTask('vagrantHalt', '--no-provision')
			println("vagrantHalt run output: " + result.output)
			assertThat(result.output).contains('VM not created. Moving on')
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	@Test
	void vagrantRecreate() {
		try {
			BuildResult result = super.runVagrantTask('vagrantRecreate', '--no-provision')
			println("vagrantRecreate run output: " + result.output)
			assertThat(result.output).contains('VM not created. Moving on').contains('Machine booted and ready!')
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	@Test
	void vagrantRecreateMaxPower() {
		try {
			BuildResult result = super.runVagrantTask('vagrantRecreateMaxPower', '--no-provision')
			println("vagrantRecreateMaxPower run output: " + result.output)
			assertThat(result.output).contains('VM not created. Moving on').contains('Machine booted and ready!')
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}

	@Test
	void vagrantDestroy() {
		try {
			BuildResult result = super.runVagrantTask('vagrantDestroy', '--no-provision')
			println("vagrantDestroy run output: " + result.output)
			assertThat(result.output).contains('VM not created. Moving on')
		} finally {
			TestUtils.vagrantCleanUp(super.testProjectDir.toFile())
		}
	}
	
	
}
