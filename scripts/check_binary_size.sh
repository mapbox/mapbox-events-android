#!/usr/bin/env bash

set -e
set -o pipefail

file_path="$1"
label="$2"
file_size=$(wc -c <"$file_path" | sed -e 's/^[[:space:]]*//')

# Publish to github
scripts/publish_to_sizechecker.js "$file_size" "$label"