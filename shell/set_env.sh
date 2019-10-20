#!/bin/bash

export APP_DIR=~/commonsos-api
export PID_FILE=~/commonsos-api.pid
export LOG_DIR=~/logs

source ~/.local_environment

[ -d $LOG_DIR ] || mkdir -p $LOG_DIR

