/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;

public class zAmazonConfigXXX {

	private final ConfigBean configBean;

	public zAmazonConfigXXX(final ConfigBean configBean) {
		this.configBean = configBean;
	}

	public AWSCredentials credentials() {

		final String username = configBean.awsAccess();
		final String password = configBean.awsSecret();

		return new BasicAWSCredentials(username, password);

	}

	public String bucket() {
		return configBean.bucket();
	}

	public String endpoint() {
		return configBean.endpoint();
	}

}
