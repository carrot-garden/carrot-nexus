#!/bin/bash
#
# Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

#
# unzip plugin bundle into the plugins folder, and then restart the service:
#
#
# nexus plugin folder
NEXUS="/var/lib/nexus/sonatype-work/plugin-repository"
#
# current plugin version
VERSION="2.2.1-build003"
#
REPO="http://repo1.maven.org/maven2"
GROUP="com/carrotgarden/nexus"
ARTIFACT="carrot-nexus-aws-s3-publish-plugin"
PLUGIN_BUNDLE="$ARTIFACT-$VERSION-bundle.zip"
#
URL="$REPO/$GROUP/$ARTIFACT/$VERSION/$PLUGIN_BUNDLE"
#
cd $NEXUS
#
service nexus stop
#
rm -r -v $ARTIFACT*
# 
wget $URL 
#
unzip $PLUGIN_BUNDLE
#
service nexus start
#
