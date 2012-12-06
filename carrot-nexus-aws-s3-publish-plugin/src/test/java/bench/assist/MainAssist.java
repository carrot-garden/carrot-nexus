/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.assist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class MainAssist {

	public static final Logger log = LoggerFactory.getLogger(MainAssist.class);

	public static void main(final String[] args) throws Exception {

		log.info("init");

		final Module module = new AbstractModule() {
			@Override
			protected void configure() {

				install(new FactoryModuleBuilder().implement(Payment.class,
						RealPayment.class).build(PaymentFactory.class));

			}
		};

		final Injector injector = Guice.createInjector(module);

		final PaymentFactory factory = injector
				.getInstance(PaymentFactory.class);

		final Payment payment = factory.create("hello");

		log.info("done");

	}

}
