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
VERSION="2.2.1-build004-SNAPSHOT"
#
REPO="http://oss.sonatype.org/content/repositories/snapshots/"
GROUP="com.carrotgarden.nexus"
ARTIFACT="carrot-nexus-aws-s3-publish-plugin"
CLASSIFIER="bundle"
PLUGIN_BUNDLE="$ARTIFACT-$VERSION-bundle.zip"
#
cd $NEXUS
#
service nexus stop
#
rm -r -v $ARTIFACT*
# 
mvn \
  org.apache.maven.plugins:maven-dependency-plugin:2.6:get \
  --define groupId=$GROUP \
  --define artifactId=$ARTIFACT \
  --define version=$VERSION \
  --define classifier=$CLASSIFIER \
  --define packaging=zip \
  --define remoteRepositories=$REPO \
  --define transitive=false \
  --define dest=$PLUGIN_BUNDLE
#
unzip $PLUGIN_BUNDLE
#
service nexus start
#
