#!/bin/bash

SCRIPT_DIR=$(cd $(dirname $0); pwd)
source $SCRIPT_DIR/set_env.sh

if [ ! -f  $PID_FILE ]
then
    echo "Pid file dose not exists."
    exit 0
fi

PID=`cat $PID_FILE`
echo "Stopping pid $PID"
kill -9 $PID
