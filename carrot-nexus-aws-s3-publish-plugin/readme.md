<!--

    Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
### info

this is a 
[nexus]
(http://www.sonatype.org/nexus/)
plugin that does amazon s3 publishing:

each time you deploy or cache an artifact into your nexus,
this same artifact is also published to the aws s3 bucket

additionally, plugin runs a task on schedule
which ensures that pre-existing artifacts are also published 

### repo

maven central has
[latest version of the plugin]
(http://search.maven.org/#search%7Cga%7C1%7Ccarrot-nexus-aws-s3-publish-plugin)

### install

unzip plugin distro in the plugin folder of your nexus, then restart
``` 
${nexus-home}/sonatype-work/nexus/plugin-repository
```

### configure

plugin expects a configuration file the in nexus config folder:
``` 
${nexus-home}/sonatype-work/nexus/conf/carrot-nexus-aws-s3-publish-plugin.conf
```
