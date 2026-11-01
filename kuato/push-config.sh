#!/usr/bin/env bash
# Push kuato.json (location for weather) to the Shield. Usage: push-config.sh <lat> <lon>
set -euo pipefail
[ $# -eq 2 ] || { echo "usage: $0 <lat> <lon>" >&2; exit 2; }
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
chmod 600 "$tmp"
printf '{"lat":%s,"lon":%s}\n' "$1" "$2" > "$tmp"
adb -s 192.168.86.239:5555 push "$tmp" /sdcard/Android/data/com.gathin.tvhome/files/kuato.json
