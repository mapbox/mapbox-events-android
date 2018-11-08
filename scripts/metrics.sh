#!/usr/bin/env bash

set -e
set -o pipefail

# Track overall library size
scripts/check_binary_size.js "libtelemetry/build/outputs/aar/libtelemetry-release.aar" "Telemetry AAR"
