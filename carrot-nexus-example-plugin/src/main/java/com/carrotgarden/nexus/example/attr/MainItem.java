package com.carrotgarden.nexus.example.attr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.attributes.StorageItemInspector;
import org.sonatype.nexus.proxy.item.StorageItem;

public class MainItem implements StorageItemInspector {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{
		log.info("###### hello ######");
	}

	@Override
	public boolean isHandled(final StorageItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processStorageItem(final StorageItem item) throws Exception {
		// TODO Auto-generated method stub

	}

}
