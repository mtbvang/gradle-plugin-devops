package com.mtbvang

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.*
import static org.junit.Assert.*;

import org.gradle.testkit.runner.BuildResult
import org.junit.Test
import org.slf4j.*

/*
 * 
 * 
 * @author Vang Nguyen
 *
 */
class VagrantTest extends FunctionalTest {

	private static Logger log = LoggerFactory.getLogger(VagrantTest.class)


	@Test
	void vagrantUp() {
		super.runVagrantTask('vagrantDestroy')
		result = runVagrantTask('vagrantUp', '--no-provision')
		assertThat(result.output).contains("Bringing machine 'centos' up with 'virtualbox' provider")
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantUp').outcome == SUCCESS
		VMInfoAsserts()
	}

	@Test
	void vagrantUpMaxPower() {
		super.runVagrantTask('vagrantDestroy')
		result = runVagrantMaxPowerTask('vagrantUpMaxPower', '--no-provision')
		assertThat(result.output).contains("Bringing machine 'centos' up with 'virtualbox' provider")
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantUpMaxPower').outcome == SUCCESS

		VMInfoMaxPowerAsserts()
	}

	@Test
	void vagrantReload() {
		ensureVMRunning()
		BuildResult result = super.runVagrantTask('vagrantReload', '--no-provision')
		assertThat(result.output).contains('Attempting graceful shutdown of VM...')
		assertThat(result.output).contains('Booting VM...')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantReload').outcome == SUCCESS
		VMInfoAsserts()
	}

	@Test
	void vagrantReloadMaxPower() {
		ensureVMRunning()
		result = super.runVagrantMaxPowerTask('vagrantReloadMaxPower', '--no-provision')
		assertThat(result.output).contains('Attempting graceful shutdown of VM...')
		assertThat(result.output).contains('Booting VM...')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified.')
		result.task(':vagrantReloadMaxPower').outcome == SUCCESS
		VMInfoMaxPowerAsserts()
	}

	@Test
	void vagrantHalt() {
		ensureVMRunning()
		result = super.runVagrantTask('vagrantHalt', '--no-provision')
		assertThat(result.output).contains('Forcing shutdown of VM...')
		result.task(':vagrantHalt').outcome == SUCCESS
	}

	@Test
	void vagrantStatus() {
		ensureVMRunning()
		result = super.runTask('vagrantStatus')
		assertThat(result.output).contains('centos                    running (virtualbox)')
		result.task(':vagrantStatus').outcome == SUCCESS
	}

	@Test
	void vagrantRecreate() {
		ensureVMRunning()
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
		
		VMInfoAsserts()

	}

	@Test
	void vagrantRecreateMaxPower() {
		ensureVMRunning()
		result = super.runVagrantMaxPowerTask('vagrantRecreateMaxPower', '--no-provision')
		isVMProvisioned = false
		assertThat(result.output).contains('Destroying VM and associated drives...')
		assertThat(result.output).contains('Machine booted and ready!')
		assertThat(result.output).contains('Machine not provisioned because `--no-provision` is specified')
		result.task(':vagrantRecreateMaxPower').outcome == SUCCESS

		// Test for task ordering. VM should be in running state after recreate
		result = super.runTask('vagrantStatus')
		assertThat(result.output).contains('running (virtualbox)')
		result.task(':vagrantStatus').outcome == SUCCESS

		VMInfoMaxPowerAsserts()
	}

	@Test
	void vagrantDestroy() {
		ensureVMRunning()
		VMInfoAsserts()
		result = super.runVagrantTask('vagrantDestroy')
		assertThat(result.output).contains('Destroying VM and associated drives...')
		result.task(':vagrantDestroy').outcome == SUCCESS
	}

	@Test
	void vagrantProvision() {
		result = super.runVagrantTask('vagrantProvision')
		assertThat(result.output).contains('VM not created. Moving on...')
		result.task(':vagrantProvision').outcome == SUCCESS
	}

	void VMInfoAsserts() {
		//Check that vm has correct max cpu and ram settings
		def vmInfo= "vboxmanage showvminfo ${testProjectDir.toFile().getName()}".execute().getText()
		assertThat(vmInfo).contains("Number of CPUs:  1")
		assertThat(vmInfo).contains("Memory size:     1024MB")
	}

	void VMInfoMaxPowerAsserts() {
		//Check that vm has correct max cpu and ram settings
		def vmInfo= "vboxmanage showvminfo ${testProjectDir.toFile().getName()}".execute().getText()
		assertThat(vmInfo).contains("Number of CPUs:  2")
		assertThat(vmInfo).contains("Memory size:     8192MB")
	}
}
