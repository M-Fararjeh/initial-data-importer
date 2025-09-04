#!/bin/sh

# Replace environment variables in env.js
# Use default value if API_BASE_URL is not set
API_BASE_URL=${API_BASE_URL:-"http://localhost:8080/data-import"}

echo "Substituting environment variables..."
echo "API_BASE_URL: $API_BASE_URL"

# Create the env.js file with the actual environment variable
cat > /usr/share/nginx/html/assets/env.js << EOF
// Environment configuration for runtime
(function(window) {
  window["env"] = window["env"] || {};

  // Environment variables
  window["env"]["API_BASE_URL"] = "$API_BASE_URL";
})(this);
EOF

echo "Environment variables substituted successfully"
echo "Generated env.js content:"
cat /usr/share/nginx/html/assets/env.js