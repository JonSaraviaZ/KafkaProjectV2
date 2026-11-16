#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
docker compose up -d
for i in $(seq 1 30); do
  if docker compose exec -T kafka kafka-topics --bootstrap-server kafka:29092 --list >/dev/null 2>&1; then break; fi
  sleep 2
done
for spec in "emergencias:3:604800000" "emergencias-auditoria:2:2592000000"; do
  IFS=: read -r topic partitions retention <<< "$spec"
  docker compose exec -T kafka kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic "$topic" --partitions "$partitions" --replication-factor 1
  docker compose exec -T kafka kafka-configs --bootstrap-server kafka:29092 --entity-type topics --entity-name "$topic" --alter --add-config "retention.ms=$retention,cleanup.policy=delete"
  docker compose exec -T kafka kafka-topics --bootstrap-server kafka:29092 --describe --topic "$topic"
  docker compose exec -T kafka kafka-configs --bootstrap-server kafka:29092 --entity-type topics --entity-name "$topic" --describe
 done
