#!/bin/sh

# Replace environment variables in env.js
envsubst < /usr/share/nginx/html/assets/env.js > /tmp/env.js && mv /tmp/env.js /usr/share/nginx/html/assets/env.js

echo "Environment variables substituted:"
echo "API_BASE_URL: ${API_BASE_URL}"