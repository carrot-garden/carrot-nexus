#!/bin/bash
#
# unzip plugin bundle into the plugins folder, and then restart the service:
#
#
# nexus plugin folder
NEXUS="/var/lib/nexus/sonatype-work/plugin-repository"
#
# current plugin version
VERSION="2.2.1-build002"
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
