package com.mtbvang

import java.io.File

import org.slf4j.*

class TestUtils {
	
	private static Logger log = LoggerFactory.getLogger(TestUtils.class)

	static vagrantCleanUp(File tmpWorkingDir) {
		println("Cleaning up vagrant with destroy in ${tmpWorkingDir}")
		// Clean up vagrant tmpWorkingDir
		executeOnShell("vagrant destroy -f", tmpWorkingDir)
		executeOnShell("VBoxManage unregistervm ${tmpWorkingDir.getName()} --delete", tmpWorkingDir)
		executeOnShell("rm -rf ${tmpWorkingDir}")
	}

	/*
	 * Lifted from http://www.joergm.com/2010/09/executing-shell-commands-in-groovy/
	 */
	static int executeOnShell(String command) {
		return executeOnShell(command, new File(System.properties.'user.dir'))
	}

	static int executeOnShell(String command, File workingDir) {
		println command
		def process = new ProcessBuilder(addShellPrefix(command))
				.directory(workingDir)
				.redirectErrorStream(true)
				.start()
		process.inputStream.eachLine { println it }
		process.waitFor();
		return process.exitValue()
	}

	static private String[] addShellPrefix(String command) {
		String[] commandArray = new String[3]
		commandArray[0] = "sh"
		commandArray[1] = "-c"
		commandArray[2] = command
		return commandArray
	}

	static String camcelCase(String text) {
		text.replaceAll(/-(.)/, { match ->
			match[1].toUpperCase()
		})
	}

	static int getRandomPort(int max, int min) {
		Random random = new Random()
		int randomPort
		
		while ({
			randomPort = random.nextInt(max + 1 - min) + min
			!available(randomPort)
		}()) continue
		
		randomPort
	}

	private static boolean available(int port) {
		try {
			Socket ignored = new Socket("127.0.0.1", port).withCloseable { }
			println("Port taken returning false: ${ignored}")
			return false
		} catch (IOException ignored) {
			println("Port available returning: ${port}")
			return true
		}
	}
}
