window.addEventListener('load', function() {
    const hash = window.location.hash.substring(1);
    const params = new URLSearchParams(hash);
    const token = params.get('token');

    if (typeof SwaggerUIBundle === "undefined") {
        console.error("SwaggerUIBundle is not loaded");
        return;
    }

    SwaggerUIBundle({
        url: "/v3/api-docs",
        dom_id: "#swagger-ui",
        presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
        layout: "StandaloneLayout",
        requestInterceptor: function(req) {
            if (token) req.headers["Authorization"] = "Bearer " + token;
            return req;
        }
    });

    // Clean URL fragment
    if (token) {
        history.replaceState(null, null, window.location.pathname);
    }
});



