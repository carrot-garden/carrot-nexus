package temp;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;

public class AmazonConfig {

	private final ConfigBean configBean;

	public AmazonConfig(final ConfigBean configBean) {
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
