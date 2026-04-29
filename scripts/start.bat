@echo off

if "%~1"=="" (
  echo Blad: Nie podano portu.
  echo Uzycie: start.bat ^<PORT^>
  exit /b 1
)

set APP_PORT=%~1

echo Uruchamianie systemu na porcie %APP_PORT%...
docker-compose up --build -d --scale app=3

echo.
echo System uruchomiony!
echo - Glowny punkt wejscia: http://localhost:%APP_PORT%
echo - Architektura: 1x Load Balancer, 3x App Node, 1x PostgreSQL