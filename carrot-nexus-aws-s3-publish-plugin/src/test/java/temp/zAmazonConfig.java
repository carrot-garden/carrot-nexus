/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

public interface zAmazonConfig {

	String PROPS_FILE = "carrot-nexus-aws-s3-publish-plugin.properties";

	String KEY_CRED_USER = "credentials.username";
	String KEY_CRED_PASS = "credentials.password";
	String KEY_BUCKET_NAME = "bucket.name";
	String KEY_AMAZON_ENDPOINT = "amazon.endpoint";
	String KEY_HEALTH_PERIOD = "health.period";

	AWSCredentials credentials();

	String bucket();

	String endpoint();

	AmazonS3Client client();

	long healthPeriod();

}
