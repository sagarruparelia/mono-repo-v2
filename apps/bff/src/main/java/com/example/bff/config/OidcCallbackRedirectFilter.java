package com.example.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

/**
 * Filter that handles two routing scenarios:
 *
 * <ol>
 *   <li><b>OIDC Callback:</b> If {@code code} and {@code state} params are present in the URL,
 *       redirects to Spring Security's OAuth2 callback endpoint ({@code /login/oauth2/code/hsid}).
 *       This handles cases where HSID redirects to the root URL or a frontend route.</li>
 *
 *   <li><b>Frontend Routing:</b> For non-API requests without OIDC params, redirects to the
 *       frontend application. This enables the BFF to serve as the entry point while delegating
 *       UI routes to the frontend.</li>
 * </ol>
 *
 * <h3>Request Flow:</h3>
 * <pre>
 * Request with ?code=...&state=...  →  Redirect to /login/oauth2/code/hsid?code=...&state=...
 * Request to /api/**                →  Pass through (handled by backend)
 * Request to /actuator/**           →  Pass through (handled by backend)
 * Request to /login/oauth2/**       →  Pass through (handled by Spring Security)
 * Request to /oauth2/**             →  Pass through (handled by Spring Security)
 * All other requests                →  Redirect to frontend UI
 * </pre>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OidcCallbackRedirectFilter implements WebFilter {

    private static final String OAUTH2_CALLBACK_PATH = "/login/oauth2/code/hsid";

    private static final Set<String> BACKEND_PATH_PREFIXES = Set.of(
            "/api/",
            "/actuator/",
            "/login/oauth2/",
            "/oauth2/"
    );

    @Value("${app.frontend-redirect-path}")
    private String frontendRedirectPath;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Check for OIDC callback params
        String code = request.getQueryParams().getFirst("code");
        String state = request.getQueryParams().getFirst("state");

        if (code != null && state != null) {
            // Has OIDC callback params - redirect to Spring Security's callback endpoint
            if (!path.startsWith("/login/oauth2/code/")) {
                return redirectToOAuth2Callback(exchange, code, state);
            }
            // Already at callback path, let Spring Security handle it
            return chain.filter(exchange);
        }

        // Check if this is a backend path
        if (isBackendPath(path)) {
            return chain.filter(exchange);
        }

        // Not a backend path and no OIDC params - redirect to frontend
        return redirectToFrontend(exchange);
    }

    private boolean isBackendPath(String path) {
        // Exact match for root actuator or API paths
        if ("/actuator".equals(path) || "/api".equals(path)) {
            return true;
        }

        // Check prefixes
        for (String prefix : BACKEND_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private Mono<Void> redirectToOAuth2Callback(ServerWebExchange exchange, String code, String state) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FOUND);

        URI redirectUri = UriComponentsBuilder.fromPath(OAUTH2_CALLBACK_PATH)
                .queryParam("code", code)
                .queryParam("state", state)
                .build()
                .toUri();

        response.getHeaders().setLocation(redirectUri);
        return response.setComplete();
    }

    private Mono<Void> redirectToFrontend(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(frontendRedirectPath));
        return response.setComplete();
    }
}
