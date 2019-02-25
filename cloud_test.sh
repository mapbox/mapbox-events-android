#!/bin/bash 
set -xe

test_path="$1"

gcloud firebase test android models list
gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/full/debug/app-full-debug.apk \
    --test "$test_path" \
    --device model=sailfish,version=28,locale=en,orientation=portrait \
    --environment-variables coverage=true,coverageFile="/sdcard/coverage.ec" \
    --directories-to-pull /sdcard \
    --timeout 20m

bucket=`gsutil ls gs://test-lab-r47d1tyt8h0hm-iku3c1i8kjrux | tail -1`
coverageFile=`gsutil ls ${bucket}sailfish-28-en-portrait/artifacts/coverage.ec`
gsutil cp $coverageFile libcore/build