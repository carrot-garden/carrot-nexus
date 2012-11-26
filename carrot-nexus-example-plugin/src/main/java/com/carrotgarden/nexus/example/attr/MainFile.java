package com.carrotgarden.nexus.example.attr;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public class MainFile implements StorageFileItemInspector {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public Set<String> getIndexableKeywords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHandled(final StorageItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processStorageFileItem(final StorageFileItem item,
			final File file) throws Exception {

		final ResourceStoreRequest request = item.getResourceStoreRequest();

		final RequestContext context = request.getRequestContext();

		// TODO Auto-generated method stub

	}

}
