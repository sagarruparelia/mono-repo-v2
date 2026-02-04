package com.example.bff.controller;

import com.example.bff.model.SessionInfo;
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

        SessionInfo sessionInfo = null;
        if (oidcUser instanceof EnrichedOidcUser enrichedUser) {
            sessionInfo = enrichedUser.getSessionInfo();
        }

        return Mono.just(SessionResponse.authenticated(userInfo, sessionInfo));
    }

    public record SessionResponse(
            boolean authenticated,
            UserInfo user,
            SessionInfo sessionInfo
    ) {
        public static SessionResponse unauthenticated() {
            return new SessionResponse(false, null, null);
        }

        public static SessionResponse authenticated(UserInfo user, SessionInfo sessionInfo) {
            return new SessionResponse(true, user, sessionInfo);
        }
    }
}
