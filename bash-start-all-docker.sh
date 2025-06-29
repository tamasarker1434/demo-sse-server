#!/bin/bash
# Bash script to start all Docker Compose stacks in parallel

(docker compose up --build -d)
wait
echo "Docker Compose services started!"
