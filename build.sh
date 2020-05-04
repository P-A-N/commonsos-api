#!/bin/bash
#. ~/develop/.test_environment
gradle clean bundle

git rev-parse HEAD > revision.txt
