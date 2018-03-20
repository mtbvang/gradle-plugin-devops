package com.mtbvang

import com.hubspot.jinjava.Jinjava

import java.nio.file.Path
import java.util.Map

import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.yaml.snakeyaml.Yaml
import org.slf4j.*
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*

import org.junit.Before
import org.junit.BeforeClass
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import java.nio.file.*

import java.nio.file.attribute.*

class IntegrationTest {
	private static Logger log = LoggerFactory.getLogger(IntegrationTest.class)
	// FIXME revert back to using TemporaryFolder to get auto clean up
	//@ClassRule public static final TemporaryFolder testProjectDir = new TemporaryFolder();
	public final Path testProjectDir = Files.createTempDirectory('devtool')
	//public final Path testProjectDir = new File('/tmp/devtoolTesting').toPath()
	
	@Before
	void setup() {
		println("Using testProjectDir: ${testProjectDir}")

	}

}
