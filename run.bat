@echo off
echo Starting FinSight AI services...

REM Copy the environment file if it doesn't exist
if not exist .env (
  echo Creating .env from .env.example...
  copy .env.example .env
)

REM Start the Docker Compose stack
docker compose up -d --build

echo.
echo FinSight AI is starting up!
echo It may take a few moments for all services to initialize.
echo Once ready, the frontend will be available at: http://localhost:3000
echo Eureka dashboard will be at: http://localhost:8761
pause
