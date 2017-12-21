package com.mtbvang

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import java.util.stream.Collectors

import static org.assertj.core.api.Assertions.assertThat
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

//@RunWith(Parameterized.class)
class DeployEachServiceFromNexusTest extends FunctionalTest {

//    @Before
//    void setup() {
//        setupTestCases("openshiftLogin")
//        setupTestCases("openshiftDeleteProject")
//        setupTestCases("openshiftCreateProject")
//    }
//
//    @Parameterized.Parameters(name = "{index}: applicationName={0}")
//    static Collection<String[]> data() {
//        def application = getConfig().get("apps").collect { camcelCase("-${it.name}") } - ["MssqlServerFpp"]
//        return application.stream().map { [it, SUCCESS] }.collect(Collectors.toList())
//    }
//
//    private List applicationName
//
//    DeployEachServiceFromNexusTest(List applicationName) {
//        this.applicationName = applicationName
//    }
//
//    @Test
//    void shouldDeployEachApplicationToEnvironmentFromNexus() {
//
//        sleep(3000)
//
//        def list = applicationName[0]
//        def outcome = applicationName[1]
//        BuildResult result = GradleRunner.create()
//                .withProjectDir("." as File)
//                .withArguments("openshiftLogin", "deploy${list}FromNexus", "openshiftPortForward${list}")
//                .build()
//
//        assertThat(result.task(":deploy${list}FromNexus").getOutcome()).isEqualTo(outcome)
//        assertThat(result.task(":openshiftPortForward${list}").getOutcome()).isEqualTo(outcome)
//    }
}
