#!/bin/bash
# Bash script to start all Docker Compose stacks in parallel

(docker compose down -v)
wait
echo "All Docker Compose services Stoped!"
