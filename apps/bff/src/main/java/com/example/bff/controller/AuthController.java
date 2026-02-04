package com.example.bff.controller;

import com.example.bff.model.UserInfo;
import com.example.bff.security.EnrichedOidcUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/login")
    public Mono<Void> login(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/oauth2/authorization/hsid"));
        return response.setComplete();
    }

    @GetMapping("/session")
    public Mono<SessionResponse> getSession(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return Mono.just(SessionResponse.unauthenticated());
        }

        UserInfo userInfo = new UserInfo(
            oidcUser.getSubject(),
            oidcUser.getEmail(),
            oidcUser.getFullName()
        );

        Map<String, Object> additionalData = Map.of();
        if (oidcUser instanceof EnrichedOidcUser enrichedUser) {
            additionalData = enrichedUser.getAdditionalAttributes();
        }

        return Mono.just(SessionResponse.authenticated(userInfo, additionalData));
    }

    public record SessionResponse(
            boolean authenticated,
            UserInfo user,
            Map<String, Object> additionalData
    ) {
        public static SessionResponse unauthenticated() {
            return new SessionResponse(false, null, Map.of());
        }

        public static SessionResponse authenticated(UserInfo user, Map<String, Object> additionalData) {
            return new SessionResponse(true, user, additionalData);
        }
    }
}
