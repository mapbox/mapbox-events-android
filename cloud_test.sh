#!/bin/bash 
set -xe

build_dir="$1"
module=$(echo "$build_dir" | cut -d "/" -f1)
test_apk_path="$2"
results_dir="$3"
is_release="$4"

devices="--device model=m0,version=18,locale=en,orientation=portrait \
--device model=hammerhead,version=21,locale=en,orientation=portrait  \
--device model=hammerhead,version=23,locale=en,orientation=landscape \
--device model=sailfish,version=26,locale=en,orientation=portrait \
--device model=sailfish,version=28,locale=en,orientation=portrait \
"
if [[ "$is_release" == "true" ]]; then
echo "Is release, adding more devices"
devices="--device model=g3,version=19,locale=en,orientation=portrait \
--device model=Nexus6,version=21,locale=en,orientation=portrait \
--device model=Nexus6,version=22,locale=en,orientation=portrait \
--device model=Nexus6,version=23,locale=en,orientation=portrait \
--device model=j1acevelte,version=22,locale=en,orientation=portrait \
--device model=sailfish,version=25,locale=en,orientation=portrait \
--device model=sailfish,version=27,locale=en,orientation=portrait \
--device model=starqlteue,version=26,locale=en,orientation=portrait \
--device model=taimen,version=26,locale=en,orientation=portrait \
--device model=taimen,version=27,locale=en,orientation=portrait \
--device model=walleye,version=26,locale=en,orientation=portrait \
--device model=walleye,version=27,locale=en,orientation=portrait \
--device model=walleye,version=28,locale=en,orientation=portrait \
--device model=zeroflte,version=23,locale=en,orientation=portrait \
--device model=hero2lte,version=23,locale=en,orientation=portrait \
--device model=cheryl,version=25,locale=en,orientation=portrait \
--device model=HWMHA,version=24,locale=en,orientation=portrait \
--device model=FRT,version=27,locale=en,orientation=portrait \
"
fi

gcloud firebase test android models list
gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/full/debug/app-full-debug.apk \
    --test "${build_dir}/${test_apk_path}" \
    --results-dir="$results_dir" \
    $(echo ${devices}) \
    --environment-variables coverage=true,coverageFile="/sdcard/${module}_coverage.ec" \
    --directories-to-pull /sdcard \
    --timeout 25m \
    --use-orchestrator

bucket="test-lab-r47d1tyt8h0hm-iku3c1i8kjrux"
artifacts_path="sailfish-28-en-portrait/artifacts"
if [[ "$is_release" == "true" ]]; then
artifacts_path="FRT-27-en-portrait/artifacts"
fi
covfile_path="gs://${bucket}/${results_dir}/${artifacts_path}/${module}_coverage.ec"
gsutil cp ${covfile_path} "${build_dir}/jacoco"