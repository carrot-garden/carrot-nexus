#!/bin/bash

#
# report config change between current and future nexus
#

function nexus {
	echo "$1/bin/nexus"
}
function wrapper_conf {
	echo "$1/bin/jsw/conf/wrapper.conf"
}
function jetty_xml {
	echo "$1/conf/jetty.xml"
}
function nexus_properties {
	echo "$1/conf/nexus.properties"
}

ONE="/usr/lib/nexus"
TWO="/usr/lib/nexus-2.2-01"

for call in nexus wrapper_conf jetty_xml nexus_properties ; do

	path_one=$($call $ONE)
	path_two=$($call $TWO)
	
	diff $path_one $path_two
		
done
