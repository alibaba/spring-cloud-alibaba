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

test_group_shade() {
  mvn_run_integration_test "$@" -DShadeTests -DtestForkCount=1 -DtestReuseFork=false
}

test_group_backwards_compat() {
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-backwards-compatibility.xml -DintegrationTests
  mvn_run_integration_test --retry "$@" -DBackwardsCompatTests -DtestForkCount=1 -DtestReuseFork=false
}

test_group_cli() {
  # run pulsar cli integration tests
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=pulsar-cli.xml -DintegrationTests

  # run pulsar auth integration tests
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=pulsar-auth.xml -DintegrationTests
}

test_group_function() {
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=pulsar-function.xml -DintegrationTests
}

test_group_messaging() {
  # run integration messaging tests
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=pulsar-messaging.xml -DintegrationTests
  # run integration proxy tests
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-proxy.xml -DintegrationTests
  # run integration proxy with WebSocket tests
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-proxy-websocket.xml -DintegrationTests
}

test_group_schema() {
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-schema.xml -DintegrationTests
}

test_group_standalone() {
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=pulsar-standalone.xml -DintegrationTests
}

test_group_transaction() {
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-transaction.xml -DintegrationTests
}

test_group_tiered_filesystem() {
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=tiered-filesystem-storage.xml -DintegrationTests
}

test_group_tiered_jcloud() {
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=tiered-jcloud-storage.xml -DintegrationTests
}

test_group_pulsar_connectors_thread() {
  # run integration function
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-thread.xml -DintegrationTests -Dgroups=function

  # run integration source
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-thread.xml -DintegrationTests -Dgroups=source

  # run integration sink
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-thread.xml -DintegrationTests -Dgroups=sink
}

test_group_pulsar_connectors_process() {
  # run integration function
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-process.xml -DintegrationTests -Dgroups=function

  # run integration source
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-process.xml -DintegrationTests -Dgroups=source

  # run integraion sink
  mvn_run_integration_test --retry "$@" -DintegrationTestSuiteFile=pulsar-process.xml -DintegrationTests -Dgroups=sink
}

test_group_sql() {
  mvn_run_integration_test "$@" -DintegrationTestSuiteFile=pulsar-sql.xml -DintegrationTests -DtestForkCount=1 -DtestReuseFork=false
}

test_group_pulsar_io() {
  mvn_run_integration_test --no-retry "$@" -DintegrationTestSuiteFile=pulsar-io-sources.xml -DintegrationTests -Dgroups=source
  mvn_run_integration_test --no-retry "$@" -DintegrationTestSuiteFile=pulsar-io-sinks.xml -DintegrationTests -Dgroups=sink
}

test_group_pulsar_io_ora() {
  mvn_run_integration_test --no-retry "$@" -DintegrationTestSuiteFile=pulsar-io-ora-source.xml -DintegrationTests -Dgroups=source -DtestRetryCount=0
}



echo "Test Group : $TEST_GROUP"
test_group_function_name="test_group_$(echo "$TEST_GROUP" | tr '[:upper:]' '[:lower:]')"
if [[ "$(LC_ALL=C type -t $test_group_function_name)" == "function" ]]; then
  eval "$test_group_function_name" "$@"
else
  echo "INVALID TEST GROUP"
  exit 1
fi
