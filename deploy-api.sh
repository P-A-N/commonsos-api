#!/bin/bash
set -x

DISTRIBUTION_FILE=~/commonsos-api.zip
REVISION_NO=`cat ~/revision.txt`
DEPLOY_DIR=~/commonsos-api-$REVISION_NO
APP_DIR=~/commonsos-api

echo "Unpacking to $DEPLOY_DIR"
[ -d $DEPLOY_DIR ] && rm -rf $DEPLOY_DIR

unzip "$DISTRIBUTION_FILE" -d $DEPLOY_DIR
chmod 755 $DEPLOY_DIR/shell/*.sh
chmod 755 $DEPLOY_DIR/batch/*.sh

echo "Linking current installation to $DEPLOY_DIR"
rm $APP_DIR
ln -sfv $DEPLOY_DIR $APP_DIR

pushd $APP_DIR
./shell/stop.sh
./shell/start.sh
popd

crontab $APP_DIR/cron/cron.txt

echo "Done"
