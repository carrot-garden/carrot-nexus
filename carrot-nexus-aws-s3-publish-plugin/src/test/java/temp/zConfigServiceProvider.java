/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;

//@Singleton
//@Named(ConfigServiceProvider.NAME)
class zConfigServiceProvider implements zConfigService {

	public static final String NAME = "carrot.config";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void configAdd(final ConfigBean config) {
		log.info("\n\t ### ADD {}", config);
	}

	@Override
	public void configEnable(final ConfigBean config) {
		log.info("\n\t ### ENA {}", config);
	}

	@Override
	public void configActivate(final ConfigBean config) {
		log.info("\n\t ### ACT {}", config);
	}

	@Override
	public void configPassivate(final ConfigBean config) {
		log.info("\n\t ### PAS {}", config);
	}

	@Override
	public void configDisable(final ConfigBean config) {
		log.info("\n\t ### DIS {}", config);
	}

	@Override
	public void configRemove(final ConfigBean config) {
		log.info("\n\t ### REM {}", config);
	}

}
