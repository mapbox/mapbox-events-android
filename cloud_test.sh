#!/bin/bash 
set -xe

test_path="$1"
module_name="$2"
gcloud firebase test android models list
gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/full/debug/app-full-debug.apk \
    --test "$test_path" \
    --device model=hammerhead,version=21,locale=en,orientation=portrait  \
    --device model=athene,version=23,locale=en,orientation=landscape \
    --device model=sailfish,version=26,locale=en,orientation=portrait \
    --device model=sailfish,version=28,locale=en,orientation=portrait \
    --environment-variables coverage=true,coverageFile="/sdcard/${module_name}_coverage.ec" \
    --directories-to-pull /sdcard \
    --timeout 20m