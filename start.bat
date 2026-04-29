@echo off
if "%~1"=="" (
    echo Usage: start.bat ^<port^>
    exit /b 1
)

set APP_PORT=%~1
echo Starting Stock Exchange on port %APP_PORT%...

docker-compose up -d --build
