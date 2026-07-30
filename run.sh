#!/bin/bash

# Copy the environment file if it doesn't exist
if [ ! -f .env ]; then
  echo "Creating .env from .env.example..."
  cp .env.example .env
fi

# Start the Docker Compose stack
echo "Starting FinSight AI services..."
docker compose up -d --build

echo ""
echo "FinSight AI is starting up!"
echo "It may take a few moments for all services to initialize."
echo "Once ready, the frontend will be available at: http://localhost:3000"
echo "Eureka dashboard will be at: http://localhost:8761"
