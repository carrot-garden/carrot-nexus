/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

//@Named
//@Singleton
class zCarrotFileInspector implements StorageFileItemInspector {

	public static final String NAME = "CarrotFileInspector";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	{

		log.info("init " + NAME);

	}

	@Inject
	private zAmazonService amazonService;

	@Override
	public Set<String> getIndexableKeywords() {

		final Set<String> wordSet = new HashSet<String>();

		return null;

	}

	@Override
	public boolean isHandled(final StorageItem item) {

		return item instanceof StorageFileItem;

	}

	@Override
	public void processStorageFileItem(final StorageFileItem item,
			final File file) throws Exception {

		/** see CarrotRepositoryStorage */

		// Util.processStorageFileItem(amazonService, item, file, log);

	}

}
