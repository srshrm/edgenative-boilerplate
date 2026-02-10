/**
 * CORS Proxy middleware for webpack dev server.
 *
 * Browser CORS policy blocks cross-origin requests from WASM to AEM EDS endpoints.
 * This middleware proxies requests server-side, bypassing CORS entirely.
 *
 * Usage: GET /cors-proxy?url=<encoded-target-url>
 */
const http = require('http');
const https = require('https');
const urlModule = require('url');

if (config.devServer) {
    const existingSetup = config.devServer.setupMiddlewares;

    config.devServer.setupMiddlewares = function (middlewares, devServer) {
        // Register CORS proxy endpoint
        devServer.app.use('/cors-proxy', function (req, res) {

            // Handle CORS preflight
            if (req.method === 'OPTIONS') {
                res.writeHead(204, {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
                    'Access-Control-Allow-Headers': '*',
                    'Access-Control-Max-Age': '86400'
                });
                res.end();
                return;
            }

            // Extract target URL from query parameter
            const parsed = urlModule.parse(req.url, true);
            const targetUrl = parsed.query.url;

            if (!targetUrl || (!targetUrl.startsWith('https://') && !targetUrl.startsWith('http://'))) {
                res.writeHead(400, {'Content-Type': 'text/plain'});
                res.end('Missing or invalid "url" query parameter');
                return;
            }

            const target = urlModule.parse(targetUrl);
            const transport = target.protocol === 'https:' ? https : http;

            // Build proxy request, stripping browser-specific headers
            const proxyHeaders = Object.assign({}, req.headers);
            proxyHeaders['host'] = target.host;
            delete proxyHeaders['origin'];
            delete proxyHeaders['referer'];
            delete proxyHeaders['sec-fetch-mode'];
            delete proxyHeaders['sec-fetch-site'];
            delete proxyHeaders['sec-fetch-dest'];

            const options = {
                hostname: target.hostname,
                port: target.port,
                path: target.path,
                method: req.method,
                headers: proxyHeaders
            };

            const proxyReq = transport.request(options, function (proxyRes) {
                // Forward response with permissive CORS headers
                const headers = Object.assign({}, proxyRes.headers);
                headers['access-control-allow-origin'] = '*';
                headers['access-control-allow-methods'] = 'GET, POST, OPTIONS';
                headers['access-control-allow-headers'] = '*';

                res.writeHead(proxyRes.statusCode, headers);
                proxyRes.pipe(res);
            });

            proxyReq.on('error', function (err) {
                console.error('[CORS Proxy] Error proxying', targetUrl, '-', err.message);
                res.writeHead(502, {'Content-Type': 'text/plain'});
                res.end('Proxy error: ' + err.message);
            });

            // Forward request body for POST/PUT
            if (req.method === 'POST' || req.method === 'PUT') {
                req.pipe(proxyReq);
            } else {
                proxyReq.end();
            }
        });

        // Chain existing middleware setup if any
        if (existingSetup) {
            return existingSetup(middlewares, devServer);
        }
        return middlewares;
    };
}
