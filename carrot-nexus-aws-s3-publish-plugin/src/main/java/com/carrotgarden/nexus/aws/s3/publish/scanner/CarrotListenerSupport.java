package com.carrotgarden.nexus.aws.s3.publish.scanner;

import java.io.File;

import org.sonatype.sisu.resource.scanner.helper.ListenerSupport;

public class CarrotListenerSupport extends ListenerSupport implements
		CarrotListener {

	@Override
	public boolean skipDirectory(final File directory) {
		return false;
	}

	@Override
	public boolean skipFile(final File file) {
		return false;
	}

}
