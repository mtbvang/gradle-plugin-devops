package com.mtbvang

import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarFile
import org.apache.commons.lang3.StringUtils
import org.slf4j.*
import org.apache.commons.collections4.IteratorUtils

/**
 * Lifted from https://stackoverflow.com/questions/1386809/copy-directory-from-a-jar-file
 * @author Vang Nguyen
 *
 */
public class DevtoolFileUtils {
	private static Logger log = LoggerFactory.getLogger(DevtoolFileUtils.class)

	public static boolean copyFile(final File toCopy, final File destFile) {
		try {
			return DevtoolFileUtils.copyStream(new FileInputStream(toCopy),
					new FileOutputStream(destFile));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean copyStream(final InputStream is, final File f) {
		try {
			log.info("copyStream file path: ${f.path}")
			boolean result = DevtoolFileUtils.copyStream(is, new FileOutputStream(f));
			return result
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean copyStream(final InputStream is, final OutputStream os) {
		try {
			final byte[] buf = new byte[1024];

			int len = 0;
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
			is.close();
			os.close();
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean ensureDirectoryExists(final File f) {
		return f.exists() || f.mkdir();
	}


	/**
	 * 
	 * @param originUrl
	 * @param destination 
	 * @throws Exception
	 */
	public static void copyResourcesRecursively(URL originUrl, File destination) throws Exception {
		URLConnection urlConnection = originUrl.openConnection();
		log.info("copyResourcesRecursively destination: ${destination.path}")
		if (urlConnection instanceof JarURLConnection) {
			DevtoolFileUtils.copyJarResourcesRecursively((JarURLConnection) urlConnection, destination);
		} else {
			DevtoolFileUtils.copyFilesRecusively(new File(originUrl.getPath()), destination);
		}
	}

	public static void copyJarResourcesRecursively(JarURLConnection jarConnection, File destination) throws IOException {
		JarFile jarFile = jarConnection.getJarFile();
		for (JarEntry entry : IteratorUtils.asIterator(jarFile.entries())) {
			if (entry.getName().startsWith(jarConnection.getEntryName())) {
				log.info("entry.getName(): ${entry.getName()}")
				log.info("jarConnection.getEntryName(): ${jarConnection.getEntryName()}")

				log.info("jarConnection.getEntryName() parent: ${new File(jarConnection.getEntryName()).getParent()}")
				String fileName = StringUtils.removeStart(entry.getName(), new File(jarConnection.getEntryName()).getParent());
				log.info("copyJarResourcesRecursively fileName: ${fileName}, to: ${destination.path}")
				if (!entry.isDirectory()) {
					InputStream entryInputStream = null;
					try {
						entryInputStream = jarFile.getInputStream(entry);
						DevtoolFileUtils.copyStream(entryInputStream, new File(destination, fileName));
					} finally {
						DevtoolFileUtils.safeClose(entryInputStream);
					}
				} else {
					DevtoolFileUtils.ensureDirectoryExists(new File(destination, fileName));
				}
			}
		}
	}

	private static boolean copyFilesRecusively(final File toCopy,
			final File destDir) {

		log.info("copyFilesRecusively fileName: ${toCopy}, to: ${destDir.path}")

		if (!toCopy.isDirectory()) {
			log.info("Copying file: {}", toCopy.absolutePath)
			return DevtoolFileUtils.copyFile(toCopy, new File(destDir, toCopy.getName()));
		} else {
			assert destDir.isDirectory();
			log.info("Copying directory: {}", toCopy.absolutePath)
			final File newDestDir = new File(destDir, toCopy.getName());
			if (!newDestDir.exists() && !newDestDir.mkdir()) {
				return false;
			}
			for (final File child : toCopy.listFiles()) {
				if (!DevtoolFileUtils.copyFilesRecusively(child, newDestDir)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Closes the given resource (e.g. stream, reader, writer, etc.) inside a try/catch.
	 * Does nothing if stream is null.
	 */
	public static void safeClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// Silent
			}
		}
	}
}