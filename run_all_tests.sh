#!/usr/bin/env bash

sbt clean coverage test acceptance:test coverageReport
python dependencyReport.py api-subscription-fields
