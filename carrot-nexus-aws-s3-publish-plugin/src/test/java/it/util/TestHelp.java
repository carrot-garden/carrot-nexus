package it.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.sonatype.nexus.test.utils.FileTestingUtils;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigDescriptor;

public class TestHelp {

	public static boolean isOrdered(final long... source) {
		final long[] target = source.clone();
		Arrays.sort(target);
		return Arrays.equals(source, target);
	}

	public static boolean isSameFile(final File source, final File target)
			throws Exception {
		return FileTestingUtils.compareFileSHA1s(source, target);
	}

	public static void sleep(final long millis) {
		final Ready never = new Ready() {
			@Override
			public boolean isReady() {
				return false;
			}
	
		};
		sleep(millis, 1, never);
	}

	public static void sleep(final long millis, final int steps,
			final Ready state) {
		for (int k = 0; k < steps; k++) {
			if (state.isReady()) {
				return;
			}
			try {
				Thread.sleep(millis / steps);
			} catch (final Exception e) {
				return;
			}
		}
	}

	public static void sleep(final long millis, final Ready state) {
		sleep(millis, 10, state);
	}

	/** config file with amazon access */
	public static File configFile() {
	
		final String home = System.getProperty("user.home");
	
		final File folder = new File(home, ".amazon/carrotgarden");
	
		final File file = new File(folder, ConfigDescriptor.NAME + ".conf");
	
		assertTrue(file.exists());
	
		return file;
	
	}

}
