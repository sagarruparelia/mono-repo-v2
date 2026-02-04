package com.example.bff.security;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcReactiveOAuth2UserService {

    private final WebClient webClient;

    public CustomOidcUserService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest)
            .flatMap(oidcUser -> enrichUser(oidcUser, userRequest));
    }

    private Mono<OidcUser> enrichUser(OidcUser oidcUser, OidcUserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String userId = oidcUser.getSubject();

        // Fetch from multiple services in parallel
        return Mono.zip(
            fetchUserProfile(userId, accessToken),
            fetchUserPermissions(userId, accessToken)
        ).map(tuple -> {
            Map<String, Object> additionalAttributes = new HashMap<>();
            additionalAttributes.put("profile", tuple.getT1());
            additionalAttributes.put("permissions", tuple.getT2());

            return (OidcUser) new EnrichedOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                additionalAttributes
            );
        }).onErrorResume(e -> {
            // Log error but don't fail authentication
            // Return basic user if enrichment fails
            return Mono.just((OidcUser) new EnrichedOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                Map.of()
            ));
        });
    }

    private Mono<Map<String, Object>> fetchUserProfile(String userId, String accessToken) {
        // TODO: Replace with your actual profile API endpoint
        // Example:
        // return webClient.get()
        //     .uri("https://api.example.com/users/{userId}/profile", userId)
        //     .headers(h -> h.setBearerAuth(accessToken))
        //     .retrieve()
        //     .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});

        // Placeholder - returns empty map until configured
        return Mono.just(Map.of());
    }

    private Mono<Map<String, Object>> fetchUserPermissions(String userId, String accessToken) {
        // TODO: Replace with your actual permissions API endpoint
        // Example:
        // return webClient.get()
        //     .uri("https://api.example.com/users/{userId}/permissions", userId)
        //     .headers(h -> h.setBearerAuth(accessToken))
        //     .retrieve()
        //     .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});

        // Placeholder - returns empty map until configured
        return Mono.just(Map.of());
    }
}
