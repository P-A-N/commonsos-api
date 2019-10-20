#!/bin/bash

SCRIPT_DIR=$(cd $(dirname $0); pwd)
source $SCRIPT_DIR/set_env.sh

pushd $APP_DIR/libs
java -Xmx1536m -Dfile.encoding=UTF-8 -jar commonsos-api.jar >> $LOG_DIR/stdouterr.log 2>&1 &
popd

echo $! > $PID_FILE
echo "Starting process id: `cat $PID_FILE`"
