/*
 * CounterCheck frontend deployment configuration.
 *
 * Local development automatically uses http://localhost:8081/api.
 * For a hosted backend, set apiBaseUrl to its public HTTPS API base URL,
 * for example: "https://api.example.com/api".
 *
 * Leave the value empty while the project is a static-only UI preview.
 * Never put credentials or secrets in this browser-visible file.
 */
window.COUNTERCHECK_CONFIG = Object.freeze({
    apiBaseUrl: ""
});
