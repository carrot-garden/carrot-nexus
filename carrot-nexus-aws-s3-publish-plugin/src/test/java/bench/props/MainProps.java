/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainProps {

	public static final Logger log = LoggerFactory.getLogger(MainProps.class);

	public static void main(final String[] args) throws Exception {

		log.info("init");

		final Properties props = new Properties();

		final File source = new File(
				"./src/test/resources/case-01/baseTest.properties");

		final File target = new File("./target/case-01/baseTest.properties");
		target.getParentFile().mkdirs();

		final InputStream input = new FileInputStream(source);

		final OutputStream output = new FileOutputStream(target);

		props.load(input);
		input.close();

		props.store(output, null);
		output.close();

		for (final Object key : props.keySet()) {
			final Object value = props.get(key);
			log.info("" //
					+ key.getClass().getSimpleName() //
					+ "=" //
					+ value.getClass().getSimpleName() //
					+ " :: " + key + "=" + value + " ");
		}

		log.info("done");

	}
}
