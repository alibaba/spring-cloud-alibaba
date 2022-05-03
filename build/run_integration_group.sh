#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

set -e
set -o pipefail
set -o errexit

TEST_GROUP=$1
if [ -z "$TEST_GROUP" ]; then
  echo "usage: $0 [test_group]"
  exit 1
fi
shift

# runs integration tests
mvn_run_integration_test() {
  (
  RETRY=""
  # wrap with retry.sh script if next parameter is "--retry"
  if [[ "$1" == "--retry" ]]; then
    RETRY="./build/retry.sh"
    shift
  fi
  # skip wrapping with retry.sh script if next parameter is "--no-retry"
  if [[ "$1" == "--no-retry" ]]; then
    RETRY=""
    shift
  fi
  set -x

  # run the integration tests
  $RETRY mvn -B -ntp -DredirectTestOutputToFile=false -f tests/pom.xml test "$@"
  )
}
