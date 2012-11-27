package com.carrotgarden.nexus.aws.s3.publish.attribute;

import java.io.File;
import java.util.Set;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

@Named(CarrotFileInspector.NAME)
public class CarrotFileInspector implements StorageFileItemInspector {

	public static final String NAME = "CarrotFileInspector";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public Set<String> getIndexableKeywords() {

		return null;

	}

	@Override
	public boolean isHandled(final StorageItem item) {

		return true;

	}

	@Override
	public void processStorageFileItem(final StorageFileItem item,
			final File file) throws Exception {

		final ResourceStoreRequest request = item.getResourceStoreRequest();

		final String path = item.getPath();

		final String repoId = item.getRepositoryId();

		// log.info("repoId={} path={}", repoId, path);

	}

}
