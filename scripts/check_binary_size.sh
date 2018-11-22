#!/usr/bin/env bash

set -e
set -o pipefail

file_path="libtelemetry/build/outputs/aar/libtelemetry-release.aar"
file_size=$(wc -c <"$file_path" | sed -e 's/^[[:space:]]*//')
label="Telemetry AAR"
scripts_path="scripts"

# Publish to github
"$scripts_path"/publish_to_sizechecker.js "$file_size" "$label"

if [[ $CIRCLE_BRANCH == master ]] || [[ $CIRCLE_BRANCH == release-* ]]; then
  date=`date '+%Y-%m-%d'`
  utc_iso_date=`date -u +'%Y-%m-%dT%H:%M:%SZ'`
  source="mobile.binarysize"
  json_name="$scripts_path/android-binarysize.json"
  json_gz="$scripts_path/android-binarysize.json.gz"

  # Write binary size to json file
  cat >"$json_name" <<EOL
  {"sdk": "telemetry", "platform": "android", "size": ${file_size}, "created_at": "${utc_iso_date}"}
  EOL

  # Compress json file
  gzip -f "$json_name" > "$json_gz" 

  # Publish to aws
  "$scripts_path"/publish_to_aws.sh $source $date $json_gz
fi
