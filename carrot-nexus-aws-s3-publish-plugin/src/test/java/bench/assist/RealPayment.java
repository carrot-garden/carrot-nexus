/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.assist;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

public class RealPayment implements Payment {

	public static final Logger log = LoggerFactory.getLogger(RealPayment.class);

	@Inject
	public RealPayment(@Assisted final String name) {

		log.info("hello");

	}

}