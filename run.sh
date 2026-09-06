#!/bin/bash
set -e

# Copy the environment file if it doesn't exist
if [ ! -f .env ]; then
  echo "Creating .env from .env.example..."
  cp .env.example .env
fi

# Auto-detect container runtime socket (Docker or Podman) if DOCKER_HOST is not set
if [ -z "$DOCKER_HOST" ]; then
  if [ -S "/run/user/$(id -u)/podman/podman.sock" ] && ! docker info >/dev/null 2>&1; then
    export DOCKER_HOST="unix:///run/user/$(id -u)/podman/podman.sock"
    echo "Using Podman socket: $DOCKER_HOST"
  fi
fi

# Start the Docker Compose stack
echo "Starting FinSight AI services..."
docker compose up -d --build

echo ""
echo "FinSight AI is starting up!"
echo "It may take a few moments for all services to initialize."
echo "Once ready, the frontend will be available at: http://localhost:3000"
echo "Eureka dashboard will be at: http://localhost:8761"

