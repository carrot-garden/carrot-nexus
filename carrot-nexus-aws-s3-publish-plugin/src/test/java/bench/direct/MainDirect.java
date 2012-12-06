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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

public class MainDirect {

	public static final Logger log = LoggerFactory.getLogger(MainDirect.class);

	public static void main(final String[] args) throws Exception {

		log.info("init");

		final Module module = new AbstractModule() {
			@Override
			protected void configure() {

			}

			@Provides
			Account account(final Customer customer) {
				return customer.account;
			}

		};

		final Injector injector = Guice.createInjector(module);

		final Customer customer = injector.getInstance(Customer.class);
		log.info("customer=" + customer);

		final Account account = injector.getInstance(Account.class);
		log.info("account=" + account);

		log.info("done");

	}

}
