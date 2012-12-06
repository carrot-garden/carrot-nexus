/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import com.carrotgarden.nexus.aws.s3.publish.config.ConfigBean;

interface zConfigService {

	void configAdd(final ConfigBean config);

	void configEnable(final ConfigBean config);

	void configActivate(final ConfigBean config);

	void configPassivate(final ConfigBean config);

	void configDisable(final ConfigBean config);

	void configRemove(final ConfigBean config);

}
