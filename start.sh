#!/bin/bash
if [ -z "$1" ]; then
  echo "Usage: ./start.sh <port>"
  exit 1
fi

export APP_PORT=$1
echo "Starting Stock Exchange on port $APP_PORT..."

docker-compose up -d --build
