const http = require("http");
const fs = require("fs");
const path = require("path");

const FRONT_PORT = Number(process.env.FRONT_PORT || 3000);
const BACKEND_HOST = process.env.BACKEND_HOST || "localhost";
const BACKEND_PORT = Number(process.env.BACKEND_PORT || 8080);

const indexPath = path.join(__dirname, "index.html");

const server = http.createServer((request, response) => {
    if (request.url === "/" || request.url === "/index.html") {
        fs.readFile(indexPath, (error, content) => {
            if (error) {
                response.writeHead(500);
                response.end("Cannot read index.html");
                return;
            }
            response.writeHead(200, {"Content-Type": "text/html; charset=utf-8"});
            response.end(content);
        });
        return;
    }

    if (request.url.startsWith("/api/")) {
        proxyToBackend(request, response);
        return;
    }

    response.writeHead(404);
    response.end("Not found");
});

function proxyToBackend(request, response) {
    const backendPath = request.url.substring("/api".length);
    const proxyRequest = http.request({
        hostname: BACKEND_HOST,
        port: BACKEND_PORT,
        path: backendPath,
        method: request.method,
        headers: request.headers
    }, proxyResponse => {
        response.writeHead(proxyResponse.statusCode, proxyResponse.headers);
        proxyResponse.pipe(response);
    });

    proxyRequest.on("error", error => {
        response.writeHead(502, {"Content-Type": "application/json; charset=utf-8"});
        response.end(JSON.stringify({message: error.message}));
    });

    request.pipe(proxyRequest);
}

server.listen(FRONT_PORT, () => {
    console.log(`Frontend: http://localhost:${FRONT_PORT}`);
    console.log(`Backend:  http://${BACKEND_HOST}:${BACKEND_PORT}`);
});
