#!/usr/bin/env bash

sbt clean coverage test it:test acceptance:test coverageReport
python dependencyReport.py api-subscription-fields
