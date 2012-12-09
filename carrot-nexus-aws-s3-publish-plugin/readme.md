<!--

    Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
## CarrotGarden AWS S3 Publish Plugin

### info

this is a 
[sonatype nexus]
(http://www.sonatype.org/nexus/)
plugin that does amazon s3 publishing:

each time you deploy or cache an artifact into your nexus,
that artifact is also published to the aws s3 bucket;

additionally, plugin runs a task on schedule
which ensures that pre-existing artifacts are also published to the amazon;

finally, when plug-in is **enabled** and amazon s3 bucket becomes **unavailable** for any reason,
you will not be able to deploy or cache any artifacts in the plugin-configured repositories,
until that bucket becomes available again;

### repo

maven central has
[latest version of the plugin]
(http://search.maven.org/#search%7Cga%7C1%7Ccarrot-nexus-aws-s3-publish-plugin)
and you are looking for the ```bundle.zip``` artifact;

### install

you need to unzip plugin bundle into the plugins folder, and then restart the service;

for example, you could use the following
[install-release.sh]
(https://github.com/carrot-garden/carrot-nexus/blob/master/carrot-nexus-aws-s3-publish-plugin/doc/install-release.sh)
or
[install-snapshot.sh]
(https://github.com/carrot-garden/carrot-nexus/blob/master/carrot-nexus-aws-s3-publish-plugin/doc/install-snapshot.sh)
install scripts;

or you could download manually latest 
[release]
(https://oss.sonatype.org/content/repositories/releases/com/carrotgarden/nexus/carrot-nexus-aws-s3-publish-plugin/)
or
[snapshot]
(https://oss.sonatype.org/content/repositories/snapshots/com/carrotgarden/nexus/carrot-nexus-aws-s3-publish-plugin/)

### configure

you need to create aws s3 bucket and corresponding user or group credentials;

for example, here is 
[minimum required user access policy]
(https://github.com/carrot-garden/carrot-nexus/blob/master/carrot-nexus-aws-s3-publish-plugin/doc/user-policy.json)

plug-in installs **default configuration** under:
```
Nexus -> Administration -> Capabilities
``` 

which needs be configured with your amazon credentials, email address, etc.;

if you delete all plugin configurations, default configuration 
will be re-created again on nexus restart;

![default config screen]
(https://raw.github.com/carrot-garden/carrot-nexus/master/carrot-nexus-aws-s3-publish-plugin/doc/readme-01.png)

### version

this plug-in depends on appropriate **nexus** and **nexus-capabilities-plugin** versions;
when in doubt, please check ```nexus.version``` and ```nexus-capabilities.version``` in the
[project properties]
(https://github.com/carrot-garden/carrot-nexus/blob/master/carrot-nexus-aws-s3-publish-plugin/pom.xml)
of the currently downloaded plug-in bundle; 

plug-in convention is such, that in plug-in version ```2.2.1-build001``` 
the ```2.2.1``` means the base nexus version the plug-in is built against,
and ```build001``` is plugin build number;

### troubleshoot

* remember to select "Enabled" check box and do "Save" after you supplied your credentials and bucket
* navigate and refresh : ```Nexus -> Views -> System Feeds -> Error and Warning Events```
* enable plug-in status page and see what's there
* grep through nexus.log

example error message you will find in ```System Feeds``` for an invalid aws s3 access key: 
```
Status Code: 403, AWS Service: Amazon S3, 
AWS Request ID: 89FA96BD3AD3AF32, 
AWS Error Code: InvalidAccessKeyId, 
AWS Error Message: The AWS Access Key Id you provided does not exist in our records., 
S3 Extended Request ID: fMHxoVvODIa1DNqASSw4XPF9aBASw1Y+J8lHMxCgrYSvYJQ0LuU+WWowEjtvn0ip    
at com.amazonaws.http.AmazonHttpClient.handleErrorRespon..
``` 

you can also enable plug-in and then try to upload artifact via nexus gui; 
if you get a message similar to the following, 
your amazon credentials are likely incorrect:

![upload error]
(https://raw.github.com/carrot-garden/carrot-nexus/master/carrot-nexus-aws-s3-publish-plugin/doc/readme-02.png)

### build your own

you can clone this project and build your own plug-in with your changes,
following steps similar to example
[build script]
(https://github.com/carrot-garden/carrot-nexus/blob/master/carrot-nexus-aws-s3-publish-plugin/doc/build-script.sh)
