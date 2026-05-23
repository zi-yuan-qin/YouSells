#!/bin/bash
cd "$(dirname "$0")"
while true; do
  echo "[$(date)] Starting Vite dev server..."
  npm run dev
  echo "[$(date)] Vite exited, restarting in 3s..."
  sleep 3
done
