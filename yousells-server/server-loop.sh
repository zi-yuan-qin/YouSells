#!/bin/bash
cd "$(dirname "$0")"
while true; do
  echo "[$(date)] Starting backend server..."
  java -jar target/yousells-server-0.0.1-SNAPSHOT.jar
  echo "[$(date)] Backend exited, restarting in 3s..."
  sleep 3
done
