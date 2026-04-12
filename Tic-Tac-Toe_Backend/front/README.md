# Minimal backend checker

1. Start the backend on `http://localhost:8080`.
2. Start this frontend proxy:

```powershell
node front/server.js
```

3. Open:

```text
http://localhost:3000
```

The page calls `/api/*`; `server.js` proxies those requests to the backend, so no backend CORS changes are required.
