/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.nexus.aws.s3.publish.condition;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

@Named(ScheduleCondition.NAME)
public class ScheduleCondition extends ConditionSupport implements Runnable {

	public static final String NAME = "carrot.condition.schedule";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	protected ScheduleCondition(final EventBus eventBus) {

		super(eventBus);

		new Thread(this).start();

	}

	@Override
	protected void doBind() {
		getEventBus().register(this);
	}

	@Override
	protected void doRelease() {
		getEventBus().unregister(this);
	}

	@Override
	public void run() {

		while (true) {

			setSatisfied(true);

			log.info("### ON");

			sleep(5 * 1000);

			setSatisfied(false);

			log.info("### OFF");

			sleep(5 * 1000);

		}

	}

	private void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String explainSatisfied() {
		return NAME + " satisfied";
	}

	@Override
	public String explainUnsatisfied() {
		return NAME + " unsatisfied";
	}

}
