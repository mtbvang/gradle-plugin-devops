package com.mtbvang

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.*
import static org.junit.Assert.*;

import java.nio.file.Paths

import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.slf4j.*

/*
 * These tests will call the openshift tasks and actually perform the openshift actions. 
 * 
 * @author Vang Nguyen
 *
 */
class Fabric8Test extends FunctionalTest {

	private static Logger log = LoggerFactory.getLogger(Fabric8Test.class)
	
	@Before
	void setupFabric8Test() {

		println("Fabric8Test setup called.")
		ensureVMProvisioned(14336, 2, true)
		runTask('openshiftHalt')
		result = runTask('openshiftUp')
		assertThat(result.output).contains('OpenShift server started.')
		assertThat(result.output).contains('The server is accessible via web console at:')
		assertThat(result.output).contains('https://192.168.152.2:8443')
		result.task(':openshiftUp').outcome == SUCCESS
	}
	
	@Test
	void cicd() {
		result = runTask('cicd')
		assertThat(result.output).contains('Waiting, endpoint for service is not ready yet...')
		assertThat(result.output).contains('Opening URL http://fabric8-fabric8.192.168.152.2.nip.io')
		result.task(':cicd').outcome == SUCCESS
	}
	
	@Test
	void cicdFabric8() {
		result = runTask('cicdFabric8')
		assertThat(result.output).contains('Waiting, endpoint for service is not ready yet...')
		assertThat(result.output).contains('Opening URL http://fabric8-fabric8.192.168.152.2.nip.io')
		result.task(':cicdFabric8').outcome == SUCCESS
	}
	
	@Test
	void cicdGitlab() {
		result = runTask('cicdGitlab')
		assertThat(result.output).contains('')
		result.task(':cicdGitlab').outcome == SUCCESS
	}
	
	@Test
	void cicdArtifactory() {
		result = runTask('cicdArtifactory')
		assertThat(result.output).contains('')
		result.task(':cicdArtifactory').outcome == SUCCESS
	}
	
	@Test
	void cicdNexus() {
		result = runTask('cicdNexus')
		assertThat(result.output).contains('')
		result.task(':cicdNexus').outcome == SUCCESS
	}
}
