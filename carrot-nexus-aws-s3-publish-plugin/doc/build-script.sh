#!/bin/bash
#
# Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#
# work home
cd /tmp
# clone repo
git clone git@github.com:carrot-garden/carrot-nexus.git
# select repo
cd carrot-nexus
# select project
cd carrot-nexus-aws-s3-publish-plugin
# produce bundle
mvn clean install
# review result
cd target
ls -las *-bundle.zip
# 
