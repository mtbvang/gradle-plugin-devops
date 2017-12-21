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
class DeployEachServiceFromSourceControlTest extends FunctionalTest {

//    @Before
//    void setup() {
//        setupTestCases("openshiftLogin")
//        setupTestCases("openshiftDeleteProject")
//        setupTestCases("openshiftCreateProject")
//    }
//
//    @Parameterized.Parameters(name = "{index}: applicationName={0}")
//    static Collection<String[]> data() {
//        def application = getConfig().get("apps").collect { camcelCase("-${it.name}") }
//        return application.stream().map { [it, SUCCESS] }.collect(Collectors.toList())
//    }
//
//    private List applicationName
//
//    DeployEachServiceFromSourceControlTest(List applicationName) {
//        this.applicationName = applicationName
//    }
//
//    @Test
//    void shouldDeployEachApplicationToEnvironmentFromSourceControl() {
//        def list = applicationName[0]
//        def outcome = applicationName[1]
//        BuildResult result = GradleRunner.create()
//                .withProjectDir("." as File)
//                .withArguments("deploy${list}", "openshiftPortForward${list}", "undeploy${list}")
//                .build()
//
//        assertThat(result.task(":deploy${list}").getOutcome()).isEqualTo(outcome)
//        assertThat(result.task(":openshiftPortForward${list}").getOutcome()).isEqualTo(outcome)
//        assertThat(result.task(":undeploy${list}").getOutcome()).isEqualTo(outcome)
//    }
}
