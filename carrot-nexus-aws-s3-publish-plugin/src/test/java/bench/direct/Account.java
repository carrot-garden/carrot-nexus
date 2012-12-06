/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.direct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account {

	public static final Logger log = LoggerFactory.getLogger(Account.class);

	{
		log.info("account=" + this);
	}

}
