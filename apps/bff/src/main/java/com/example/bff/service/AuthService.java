package com.example.bff.service;

import com.example.bff.config.OidcProperties;
import com.example.bff.model.AuthState;
import com.example.bff.model.TokenResponse;
import com.example.bff.util.PkceUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final OidcProperties oidcProperties;
    private final SessionService sessionService;
    private final WebClient webClient;

    public AuthService(OidcProperties oidcProperties, SessionService sessionService) {
        this.oidcProperties = oidcProperties;
        this.sessionService = sessionService;
        this.webClient = WebClient.builder().build();
    }

    public Mono<AuthState> initiateLogin() {
        return Mono.fromCallable(() -> {
            String state = PkceUtil.generateState();
            String codeVerifier = PkceUtil.generateCodeVerifier();
            String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

            sessionService.storePkceVerifier(state, codeVerifier);

            String authorizationUrl = UriComponentsBuilder
                    .fromUriString(oidcProperties.getAuthorizationUri())
                    .queryParam("response_type", "code")
                    .queryParam("client_id", oidcProperties.getClientId())
                    .queryParam("redirect_uri", oidcProperties.getRedirectUri())
                    .queryParam("scope", oidcProperties.getScopes().replace(",", " "))
                    .queryParam("state", state)
                    .queryParam("code_challenge", codeChallenge)
                    .queryParam("code_challenge_method", "S256")
                    .build()
                    .toUriString();

            return new AuthState(state, codeVerifier, authorizationUrl);
        });
    }

    public Mono<TokenResponse> exchangeCodeForTokens(String code, String state) {
        return Mono.fromCallable(() -> sessionService.getPkceVerifier(state))
                .flatMap(codeVerifier -> {
                    if (codeVerifier == null) {
                        return Mono.error(new IllegalStateException("Invalid state parameter"));
                    }

                    return webClient.post()
                            .uri(oidcProperties.getTokenUri())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(BodyInserters
                                    .fromFormData("grant_type", "authorization_code")
                                    .with("code", code)
                                    .with("redirect_uri", oidcProperties.getRedirectUri())
                                    .with("client_id", oidcProperties.getClientId())
                                    .with("code_verifier", codeVerifier))
                            .retrieve()
                            .bodyToMono(TokenResponse.class);
                });
    }
}
