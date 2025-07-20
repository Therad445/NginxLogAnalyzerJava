#!/bin/sh
set -e
BRANCH=paper-artifacts
TAG=v1.2-paper
files="docker-compose.yml README_ARTIFACTS.md artifacts bench data grafana provisioning pom.xml target/LogAlertJob.jar add_paper_artifacts.sh"
git checkout -B "$BRANCH"
git add $files
git commit -m "Add reproducibility bundle v1.2"
git tag "$TAG"
