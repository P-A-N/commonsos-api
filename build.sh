#!/bin/bash
. ~/develop/.test_environment
gradle clean test bundle

git rev-parse HEAD > revision.txt
