package com.carrotgarden.nexus.aws.s3.publish.scanner;

import java.io.File;

import org.sonatype.sisu.resource.scanner.Listener;

public interface CarrotListener extends Listener {

	boolean skipDirectory(File directory);

	boolean skipFile(File file);

}
