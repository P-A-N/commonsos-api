#!/bin/bash

[ -d ../logs ] || mkdir -p ../logs

. ~/.local_environment
java -Xmx1024m -Dmode=test -Dfile.encoding=UTF-8 -jar commonsos-api.jar >> ../logs/stdouterr.log 2>&1 &

echo $! > pid
echo "Started pid $!"