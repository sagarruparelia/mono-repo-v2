package com.example.bff.controller;

import com.example.bff.config.OidcProperties;
import com.example.bff.model.Session;
import com.example.bff.model.UserInfo;
import com.example.bff.service.AuthService;
import com.example.bff.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final OidcProperties oidcProperties;

    public AuthController(AuthService authService, SessionService sessionService, OidcProperties oidcProperties) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.oidcProperties = oidcProperties;
    }

    @GetMapping("/login")
    public Mono<ResponseEntity<Void>> login(ServerHttpResponse response) {
        return authService.initiateLogin()
                .map(authState -> {
                    response.addCookie(
                            ResponseCookie.from("AUTH_STATE", authState.state())
                                    .httpOnly(true)
                                    .secure(false)
                                    .sameSite("Lax")
                                    .path("/")
                                    .maxAge(300)
                                    .build()
                    );

                    return ResponseEntity
                            .status(HttpStatus.FOUND)
                            .location(URI.create(authState.authorizationUrl()))
                            .build();
                });
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<Void>> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @CookieValue(value = "AUTH_STATE", required = false) String storedState,
            ServerHttpResponse response) {

        if (storedState == null || !state.equals(storedState)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return authService.exchangeCodeForTokens(code, state)
                .flatMap(tokens -> sessionService.createSession(tokens))
                .<ResponseEntity<Void>>map(session -> {
                    response.addCookie(
                            ResponseCookie.from("SESSION_ID", session.getId())
                                    .httpOnly(true)
                                    .secure(false)
                                    .sameSite("Lax")
                                    .path("/")
                                    .maxAge(3600)
                                    .build()
                    );

                    response.addCookie(
                            ResponseCookie.from("AUTH_STATE", "")
                                    .httpOnly(true)
                                    .path("/")
                                    .maxAge(0)
                                    .build()
                    );

                    return ResponseEntity
                            .status(HttpStatus.FOUND)
                            .location(URI.create(oidcProperties.getFrontendRedirectUri()))
                            .build();
                })
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                ));
    }

    @GetMapping("/session")
    public Mono<ResponseEntity<SessionResponse>> getSession(
            @CookieValue(value = "SESSION_ID", required = false) String sessionId) {

        if (sessionId == null) {
            return Mono.just(ResponseEntity.ok(new SessionResponse(false, null)));
        }

        return sessionService.validateSession(sessionId)
                .map(session -> {
                    if (session == null) {
                        return ResponseEntity.ok(new SessionResponse(false, null));
                    }
                    return ResponseEntity.ok(new SessionResponse(true, session.getUserInfo()));
                })
                .defaultIfEmpty(ResponseEntity.ok(new SessionResponse(false, null)));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(
            @CookieValue(value = "SESSION_ID", required = false) String sessionId,
            ServerHttpResponse response) {

        Mono<Void> destroySession = sessionId != null
                ? sessionService.destroySession(sessionId)
                : Mono.empty();

        return destroySession
                .then(Mono.fromRunnable(() -> {
                    response.addCookie(
                            ResponseCookie.from("SESSION_ID", "")
                                    .httpOnly(true)
                                    .path("/")
                                    .maxAge(0)
                                    .build()
                    );
                }))
                .thenReturn(ResponseEntity.ok().build());
    }

    public record SessionResponse(boolean authenticated, UserInfo user) {
    }
}
