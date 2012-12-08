package com.carrotgarden.nexus.aws.s3.publish.amazon;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigEntry;

public interface AmazonFactory {

	AmazonManager create(ConfigEntry entry);

}
