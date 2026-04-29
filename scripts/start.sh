#!/bin/bash

if [ -z "$1" ]; then
  echo "Błąd: Nie podano portu."
  echo "Użycie: ./start.sh <PORT>"
  exit 1
fi

export APP_PORT=$1

echo "Uruchamianie systemu na porcie $APP_PORT..."
docker-compose up --build -d --scale app=3

echo "System uruchomiony!"
echo "- Główny punkt wejścia: http://localhost:$APP_PORT"
echo "- Architektura: 1x Load Balancer, 3x App Node, 1x PostgreSQL"