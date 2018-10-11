#!/bin/bash
set -xe

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

"$PROJECT_DIR"/gradlew --no-daemon --info clean build -PdisablePreDex
"$PROJECT_DIR"/test.sh