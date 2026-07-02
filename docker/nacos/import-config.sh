#!/bin/sh
set -e
NACOS="${NACOS_SERVER:-http://nacos:8848}"
echo "Waiting for Nacos..."
for i in $(seq 1 30); do
  if curl -sf "$NACOS/nacos/v1/console/health/readiness" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done
for f in /nacos-config/*.yaml; do
  [ -f "$f" ] || continue
  name=$(basename "$f" .yaml)
  echo "Importing $name ..."
  curl -sf -X POST "$NACOS/nacos/v1/cs/configs" \
    -F "dataId=${name}.yaml" \
    -F "group=DEFAULT_GROUP" \
    -F "type=yaml" \
    -F "content=<$f" || true
done
echo "Nacos config import done."
