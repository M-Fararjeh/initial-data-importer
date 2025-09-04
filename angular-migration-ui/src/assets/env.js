// Environment configuration for runtime
(function(window) {
  window["env"] = window["env"] || {};

  // Environment variables - these will be replaced by Docker at runtime
  window["env"]["API_BASE_URL"] = "http://localhost:8080/data-import";
})(this);