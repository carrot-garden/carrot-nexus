package com.carrotgarden.nexus.aws.s3.publish.scanner;

import java.io.File;

public interface CarrotScanner {

	void scan(File directory, CarrotListener listener);

}
