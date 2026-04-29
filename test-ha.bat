@echo off

if "%~1"=="" (
  echo Error: Port not provided.
  echo Usage: test-ha.bat ^<PORT^>
  exit /b 1
)

set PORT=%~1

echo --- 1. Current State ---
curl -s -w "\nHTTP: %%{http_code}\n" http://localhost:%PORT%/stocks
echo.

echo --- 2. Triggering /chaos ---
curl -s -i -X POST http://localhost:%PORT%/chaos
echo.
echo.

echo --- 3. Immediate Failover Check ---
curl -s -w "\nHTTP: %%{http_code}\n" http://localhost:%PORT%/stocks
echo.