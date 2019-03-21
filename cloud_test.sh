#!/bin/bash 
set -xe
module_name="$1"
build_dir_outputs="$2"
test_apk_path="$3"
gcloud firebase test android models list
gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/full/debug/app-full-debug.apk \
    --test "${build_dir_outputs}/${test_apk_path}" \
    --device model=hammerhead,version=21,locale=en,orientation=portrait  \
    --device model=athene,version=23,locale=en,orientation=landscape \
    --device model=sailfish,version=26,locale=en,orientation=portrait \
    --device model=sailfish,version=28,locale=en,orientation=portrait \
    --environment-variables coverage=true,coverageFile="/sdcard/${module_name}_coverage.ec" \
    --directories-to-pull /sdcard \
    --timeout 20m

covfile_path="gs://test-lab-r47d1tyt8h0hm-iku3c1i8kjrux/sailfish-28-en-portrait/artifacts/${module_name}_coverage.ec"
gsutil cp covfile_path "${build_dir_outputs}/code_coverage"