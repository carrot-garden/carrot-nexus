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

finally, when plug-in is **enabled** and amazon s3 bucket becomes unavailable for any reason,
you will not be able to deploy or cache any artifacts in the affected/configured repositories,
until that bucket becomes available again; bucket availability status is checked periodically;

### repo

maven central has
[latest version of the plugin]
(http://search.maven.org/#search%7Cga%7C1%7Ccarrot-nexus-aws-s3-publish-plugin)
and you are looking for the ```bundle.zip``` artifact;

### nexus

this plug-in depends on appropriate **nexus** and **nexus-capabilities-plugin** versions;
when in doubt, please check ```nexus.version``` and ```nexus-capabilities.version``` in the
[project descriptor]
(https://github.com/carrot-garden/carrot-nexus/blob/master/carrot-nexus-aws-s3-publish-plugin/pom.xml)
of the currently downloaded plug-in bundle;

### install

``` 
#!/bin/bash
#
# unzip plugin bundle into the plugins folder, and then restart the service:
#
# nexus work folder
NEXUS="/var/lib/nexus"
# current plugin version
VERSION="2.2.1-build001"
#
REPO="http://repo1.maven.org/maven2"
GROUP="com/carrotgarden/nexus"
ARTIFACT="carrot-nexus-aws-s3-publish-plugin"
PLUGIN_BUNDLE="$ARTIFACT-$VERSION-bundle.zip"
URL="$REPO/$GROUP/$ARTIFACT/$VERSION/$PLUGIN_BUNDLE"
#
cd $NEXUS/sonatype-work/nexus/plugin-repository
wget $URL 
unzip $PLUGIN_BUNDLE
service nexus restart

```

### configure

plug-in installs **default configuration** under:
```
Nexus -> Administration -> Capabilities
``` 

which needs be configured with your amazon credentials, email address, etc.;

if you delete all plugin configurations, default configuration 
will be re-created again on nexus restart;

![default config screen]
(https://raw.github.com/carrot-garden/carrot-nexus/master/carrot-nexus-aws-s3-publish-plugin/doc/readme-01.png)

### troubleshoot

* remember to select "Enabled" check box and do "Save" after you supplied your credentials
* navigate and refresh : ```Nexus -> Views -> System Feeds -> Error and Warning Events```
* verify state of "Active" check box and pay attention to message there, if any
* click "Refresh" to see if "Active" state changed
* reduce health check period to see changes sooner
* grep through nexus.log

example error message you will find in ```System Feeds``` for invalid aws s3 access key: 
```
### amazon provider unavailable
Status Code: 403, AWS Service: Amazon S3, 
AWS Request ID: 89FA96BD3AD3AF32, 
AWS Error Code: InvalidAccessKeyId, 
AWS Error Message: The AWS Access Key Id you provided does not exist in our records., 
S3 Extended Request ID: fMHxoVvODIa1DNqASSw4XPF9aBASw1Y+J8lHMxCgrYSvYJQ0LuU+WWowEjtvn0ip    
at com.amazonaws.http.AmazonHttpClient.handleErrorRespon..
``` 
