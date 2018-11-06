#!/usr/bin/env bash

set -e
set -o pipefail

# Track individual architectures
./check_binary_size.js "libtelemetry/build/intermediates/intermediate-jars/release/jni/armeabi-v7a/libmapbox-gl.so" "Android arm-v7"
./check_binary_size.js "libtelemetry/build/intermediates/intermediate-jars/release/jni/arm64-v8a/libmapbox-gl.so"   "Android arm-v8"
./check_binary_size.js "libtelemetry/build/intermediates/intermediate-jars/release/jni/x86/libmapbox-gl.so"         "Android x86"
./check_binary_size.js "libtelemetry/build/intermediates/intermediate-jars/release/jni/x86_64/libmapbox-gl.so"      "Android x86_64"

# Track overall library size
./check_binary_size.js "libtelemetry/build/outputs/aar/libtelemetry-release.aar" "Android AAR"
