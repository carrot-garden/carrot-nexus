/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import java.io.File;

public interface zAmazonService {

	/** @return last amazon health check status */
	boolean isAvailable();

	/** run amazon health check now */
	void checkAvailable();

	//

	/** delete path from s3 */
	boolean kill(String path);

	/** load file from s3 */
	boolean load(String path, File file);

	/** save file to s3 */
	boolean save(String path, File file);

}
