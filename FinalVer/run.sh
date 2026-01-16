#!/bin/sh
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

PORT="${1:-8080}"

printf "Compiling Java sources...\n"
javac Server/ServerMain.java SimilarityCalculator.java Analyzer/*.java DataStructure/*.java Constants/*.java

printf "Starting server on http://localhost:%s\n" "$PORT"
java -cp ".:Server" ServerMain "$PORT" &
SERVER_PID=$!

cleanup() {
  kill "$SERVER_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

sleep 0.4

if command -v open >/dev/null 2>&1; then
  open "http://localhost:$PORT" >/dev/null 2>&1 || true
elif command -v xdg-open >/dev/null 2>&1; then
  xdg-open "http://localhost:$PORT" >/dev/null 2>&1 || true
fi

wait "$SERVER_PID"
