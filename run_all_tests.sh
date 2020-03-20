#!/usr/bin/env bash

export SBT_OPTS="-XX:MaxMetaspaceSize=1G"
sbt clean coverage test acceptance:test coverageReport
