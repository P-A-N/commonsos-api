#!/bin/bash

source set_env.sh

java -Xmx1024m -Dfile.encoding=UTF-8 -jar $APP_DIR/commonsos-api.jar commonsos.tools.CreateNclAccount $1
