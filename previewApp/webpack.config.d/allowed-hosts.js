/**
 * Allow connections from Docker service names and external hosts.
 *
 * webpack-dev-server rejects requests whose Host header isn't "localhost" by default.
 * When Playwright (in the llm-service container) navigates to http://preview:8088,
 * the Host header is "preview" — which must be explicitly allowed.
 */
if (config.devServer) {
    config.devServer.allowedHosts = config.devServer.allowedHosts || [];
    config.devServer.allowedHosts.push('preview');   // Docker Compose service name
    config.devServer.allowedHosts.push('localhost');
    config.devServer.allowedHosts.push('.local');     // mDNS / LAN access
}