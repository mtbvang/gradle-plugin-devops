package com.mtbvang

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.*
import static org.junit.Assert.*;

import org.gradle.testkit.runner.BuildResult
import org.junit.Test
import org.slf4j.*

/*
 * The setup method that runs before all tasks ensures there's a non provisioned VM to run these tests against.
 * 
 * @author Vang Nguyen
 *
 */
class VagrantTest extends FunctionalTest {

	private static Logger log = LoggerFactory.getLogger(VagrantTest.class)


	@Test
	void vagrantUp() {
		result = runVagrantTask('vagrantUp', '--no-provision')
		assertThat(result.output).contains("Bringing machine 'centos' up with 'virtualbox' provider")
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantUp').outcome == SUCCESS
	}

	@Test
	void vagrantReload() {
		BuildResult result = super.runVagrantTask('vagrantReload', '--no-provision')
		assertThat(result.output).contains('Booting VM...')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantReload').outcome == SUCCESS
	}

	@Test
	void vagrantReloadMaxPower() {
		vagrantVMMemory = vagrantVMMemory + 1024
		vagrantVMCPUs = vagrantVMCPUs + 1
		result = super.runVagrantTask('vagrantReloadMaxPower', '--no-provision')
		assertThat(result.output).contains('Booting VM...')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantReloadMaxPower').outcome == SUCCESS

		// Could test if VM actually has max power settings by querying virtualbox
	}

	@Test
	void vagrantHalt() {
		result = super.runVagrantTask('vagrantHalt', '--no-provision')
		assertThat(result.output).contains('Forcing shutdown of VM...')
		result.task(':vagrantHalt').outcome == SUCCESS
	}

	@Test
	void vagrantStatus() {
		result = super.runTask('vagrantStatus')
		assertThat(result.output).contains('running (virtualbox)')
		result.task(':vagrantStatus').outcome == SUCCESS
	}

	@Test
	void vagrantRecreate() {
		result = super.runVagrantTask('vagrantRecreate', '--no-provision')
		isVMProvisioned = false
		assertThat(result.output).contains('Destroying VM and associated drives...')
		assertThat(result.output).contains('Machine booted and ready!')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified')
		result.task(':vagrantRecreate').outcome == SUCCESS

		// Test for task ordering. VM should be in running state after recreate
		result = super.runTask('vagrantStatus')
		assertThat(result.output).contains('running (virtualbox)')
		result.task(':vagrantStatus').outcome == SUCCESS

	}

	@Test
	void vagrantRecreateMaxPower() {
		vagrantVMMemory = vagrantVMMemory + 1024
		vagrantVMCPUs = vagrantVMCPUs + 1
		result = super.runVagrantTask('vagrantRecreateMaxPower', '--no-provision')
		isVMProvisioned = false
		assertThat(result.output).contains('Destroying VM and associated drives...')
		assertThat(result.output).contains('Machine booted and ready!')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified')
		result.task(':vagrantRecreateMaxPower').outcome == SUCCESS

		result = super.runTask('vagrantStatus')
		assertThat(result.output).contains('running (virtualbox)')
		result.task(':vagrantStatus').outcome == SUCCESS
	}

	@Test
	void vagrantDestroy() {
		result = super.runVagrantTask('vagrantDestroy')
		assertThat(result.output).contains('Destroying VM and associated drives...')
		result.task(':vagrantDestroy').outcome == SUCCESS
	}

	@Test
	void vagrantProvision() {
		runTask('vagrantHalt')
		result = super.runVagrantTask('vagrantProvision')
		assertThat(result.output).contains('VM is not currently running. Please, first bring it up with `vagrant up` then run this command.')
		result.task(':vagrantProvision').outcome == SUCCESS
	}
}
