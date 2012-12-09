package com.carrotgarden.nexus.aws.s3.publish.scanner;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named(CarrotSerialScanner.NAME)
public class CarrotSerialScanner implements CarrotScanner {

	public static final String NAME = "serial";

	@Override
	public void scan(final File directory, final CarrotListener listener) {

		if (directory == null || listener == null) {
			return;
		}

		listener.onBegin();

		recurse(directory, listener);

		listener.onEnd();

	}

	private void recurse(final File directory, final CarrotListener listener) {

		if (!directory.exists()) {
			return;
		}

		if (listener.skipDirectory(directory)) {
			return;
		}

		listener.onEnterDirectory(directory);

		final File[] files = directory.listFiles();

		if (files != null) {

			for (final File file : files) {

				if (file.isDirectory()) {

					recurse(file, listener);

				} else {

					if (listener.skipFile(file)) {
						continue;
					}

					listener.onFile(file);

				}
			}
		}

		listener.onExitDirectory(directory);

	}

}
