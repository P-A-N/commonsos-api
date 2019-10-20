#!/bin/bash

source set_env.sh

if [ ! -f  $PID_FILE ]
then
    echo "Pid file dose not exists."
    exit 0
fi

PID=`cat $PID_FILE`
echo "Stopping pid $PID"
kill -9 $PID
