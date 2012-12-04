<!--

    Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
## CarrotGarden AWS S3 Publish Plugin

### info

this is a 
[nexus]
(http://www.sonatype.org/nexus/)
plugin that does amazon s3 publishing:

each time you deploy or cache an artifact into your nexus,
that artifact is also published to the aws s3 bucket;

additionally, plugin runs a task on schedule
which ensures that pre-existing artifacts are also published; 

### repo

maven central has
[latest version of the plugin]
(http://search.maven.org/#search%7Cga%7C1%7Ccarrot-nexus-aws-s3-publish-plugin)

### install

unzip plugin distro in the plugin folder of your nexus, then restart
``` 
cd ${nexus-home}/sonatype-work/nexus/plugin-repository
wget http://repo1.maven.org/maven2/com/carrotgarden/nexus/plugins/carrot-nexus-aws-s3-publish-plugin/2.2.1-build001/carrot-nexus-aws-s3-publish-plugin-2.2.1-build001-bundle.zip
unzip carrot-nexus-aws-s3-publish-plugin-2.2.1-build001-bundle.zip
```

### configure

plug-in installs default configuration under
```
Nexus -> Administration -> Capabilities
``` 
which needs be configured with your amazon credentials, email address, etc.
[default config]
(https://raw.github.com/carrot-garden/carrot-nexus/master/carrot-nexus-aws-s3-publish-plugin/doc/readme-01.png)
