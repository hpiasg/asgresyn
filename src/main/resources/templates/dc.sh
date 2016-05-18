#!/bin/sh

dc_shell -f "#*dc_tcl*#" > /dev/null
status=$?
if [ "$status" == "3" ] ; then
	echo success
elif [ "$status" == "1" ] ; then
	echo analyze error
elif [ "$status" == "2" ] ; then
	echo elaborate error
fi
