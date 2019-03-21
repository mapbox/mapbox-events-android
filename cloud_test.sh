#!/bin/bash 
set -xe

build_dir_outputs="$1"
base_dir=$(echo "$build_dir_outputs" | cut -d "/" -f1)
test_apk_path="$2"
results_dir="$3"
gcloud firebase test android models list
gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/full/debug/app-full-debug.apk \
    --test "${build_dir_outputs}/${test_apk_path}" \
    --results-dir="$results_dir" \
    --device model=hammerhead,version=21,locale=en,orientation=portrait  \
    --device model=hammerhead,version=23,locale=en,orientation=landscape \
    --device model=sailfish,version=26,locale=en,orientation=portrait \
    --device model=sailfish,version=28,locale=en,orientation=portrait \
    --environment-variables coverage=true,coverageFile="/sdcard/${base_dir}/coverage.ec" \
    --directories-to-pull /sdcard \
    --timeout 20m

bucket="test-lab-r47d1tyt8h0hm-iku3c1i8kjrux"
artifacts_path="sailfish-28-en-portrait/artifacts/${base_dir}"
covfile_path="gs://${bucket}/${results_dir}/${artifacts_path}/coverage.ec"
gsutil cp $covfile_path "${build_dir_outputs}/code_coverage"