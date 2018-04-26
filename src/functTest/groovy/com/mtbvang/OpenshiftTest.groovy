package com.mtbvang

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.*
import static org.junit.Assert.*;

import org.junit.Test
import org.slf4j.*

/*
 * These tests will call the openshift tasks and actually perform the openshift actions. 
 * 
 * @author Vang Nguyen
 *
 */
class OpenshiftTest extends FunctionalTest {

	private static Logger log = LoggerFactory.getLogger(OpenshiftTest.class)

	@Test
	void openshiftLogin() {
		ensureVMProvisioned()
		runTask('openshiftRestart')
		result = runTask('openshiftLoginSystem')
		result.task(':openshiftLoginSystem').outcome == SUCCESS
		assertThat(result.output).contains('Logged into "https://192.168.152.2:8443" as "system:admin" using existing credentials')

		result = runTask('openshiftLoginAdmin')
		result.task(':openshiftLoginAdmin').outcome == SUCCESS
		assertThat(result.output).contains('Login successful.')
		assertThat(result.output).doesNotContain('kube-public')
		
		result = runTask('openshiftLoginDeveloper')
		result.task(':openshiftLoginDeveloper').outcome == SUCCESS
		assertThat(result.output).contains('Login successful.')
		assertThat(result.output).contains('kube-public')
		
	}

	@Test
	void openshiftUp() {
		ensureVMProvisioned()
		runTask('openshiftLoginSystem')
		result = runTask('openshiftHalt')
		result.task(':openshiftHalt').outcome == SUCCESS
		result = runTask('openshiftUp')
		assertThat(result.output).contains('OpenShift server started')
		assertThat(result.output).doesNotContain('https://hawkular-metrics-openshift-infra.')
		assertThat(result.output).contains('openshift-infra')
		assertThat(result.output).contains('cluster role "cluster-admin" added: "developer"')
		result.task(':openshiftUp').outcome == SUCCESS
	}

	@Test
	void openshiftStatus() {
		ensureVMProvisioned()
		runTask('openshiftHalt')
		result = runTask('openshiftStatus')
		result.task(':openshiftStatus').outcome == SUCCESS
		assertThat(result.output).contains('Error: OpenShift cluster is not running')
	}

	@Test
	void openshiftHalt() {

		ensureVMProvisioned()
		runTask('openshiftLoginSystem')
		result = runTask('openshiftHalt')
		result.task(':openshiftHalt').outcome == SUCCESS
	}


	@Test
	void openshiftRestart() {

		ensureVMProvisioned()
		runTask('openshiftLoginSystem')
		result = runTask('openshiftRestart')
		assertThat(result.output).contains('Starting OpenShift using openshift/origin:v3.7.0 ...')
		assertThat(result.output).contains('OpenShift server started')
		assertThat(result.output).contains('openshift-infra')
		assertThat(result.output).contains('cluster role "cluster-admin" added: "developer"')
		result.task(':openshiftRestart').outcome == SUCCESS
	}
}
