#!/bin/sh
# MIT License
set -e
files="docker-compose.yml README_ARTIFACTS.md artifacts bench data grafana provisioning pom.xml"
git add $files
git commit -m "Add reproducibility bundle v1.1"
