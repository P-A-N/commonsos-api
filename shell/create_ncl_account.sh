#!/bin/bash

SCRIPT_DIR=$(cd $(dirname $0); pwd)
source $SCRIPT_DIR/set_env.sh

pushd $APP_DIR/libs
export CLASSPATH=`ls -m *.jar | sed -e "s/,/:/g" | sed -z "s/\n//g"`
java -Xmx3072m -Dfile.encoding=UTF-8 commonsos.tools.CreateNclAccount $1
popd
