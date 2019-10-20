#!/bin/bash

SCRIPT_DIR=$(cd $(dirname $0); pwd)
source $SCRIPT_DIR/set_env.sh

java -Xmx1024m -Dfile.encoding=UTF-8 -jar $APP_DIR/commonsos-api.jar commonsos.tools.TokenMigration $1
