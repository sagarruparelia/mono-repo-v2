package com.example.bff.security;

import com.example.bff.model.DelegatePermission;
import com.example.bff.model.SessionInfo;
import com.example.bff.model.UserServiceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcReactiveOAuth2UserService {

    private final WebClient hcpWebClient;

    @Value("${app.user-service.base-url}")
    private String userServiceBaseUrl;

    @Value("${app.psn.base-url}")
    private String psnBaseUrl;

    @Value("${app.session.duration-minutes:30}")
    private int sessionDurationMinutes;

    public CustomOidcUserService(@Qualifier("hcpWebClient") WebClient hcpWebClient) {
        this.hcpWebClient = hcpWebClient;
    }

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest)
            .flatMap(oidcUser -> enrichUser(oidcUser, userRequest));
    }

    private Mono<OidcUser> enrichUser(OidcUser oidcUser, OidcUserRequest userRequest) {
        String hsidUuid = oidcUser.getSubject();
        Instant sessionStart = Instant.now();
        Instant sessionEnd = sessionStart.plus(Duration.ofMinutes(sessionDurationMinutes));

        return fetchUserInfo(hsidUuid)
            .flatMap(userInfo -> {
                if ("PR".equals(userInfo.memberType())) {
                    return fetchManagedMembers(userInfo.enterpriseId())
                        .map(members -> buildSessionInfo(userInfo, hsidUuid, sessionStart, sessionEnd, "representative", members));
                } else {
                    return Mono.just(buildSessionInfo(userInfo, hsidUuid, sessionStart, sessionEnd, "self", Map.of()));
                }
            })
            .map(sessionInfo -> createEnrichedUser(oidcUser, sessionInfo))
            .onErrorResume(e -> {
                return Mono.just(createEnrichedUser(oidcUser, minimalSessionInfo(hsidUuid, sessionStart, sessionEnd)));
            });
    }

    private Mono<UserServiceResponse> fetchUserInfo(String hsidUuid) {
        return hcpWebClient.post()
            .uri(userServiceBaseUrl + "/user-info")
            .bodyValue(Map.of("hsidUuid", hsidUuid))
            .retrieve()
            .bodyToMono(UserServiceResponse.class);
    }

    private Mono<Map<String, List<DelegatePermission>>> fetchManagedMembers(String enterpriseId) {
        return hcpWebClient.post()
            .uri(psnBaseUrl + "/managed-members")
            .bodyValue(Map.of("enterpriseId", enterpriseId))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    private SessionInfo buildSessionInfo(
            UserServiceResponse userInfo,
            String hsidUuid,
            Instant sessionStart,
            Instant sessionEnd,
            String persona,
            Map<String, List<DelegatePermission>> managedMembers) {
        return new SessionInfo(
            userInfo.enterpriseId(),
            hsidUuid,
            sessionStart,
            sessionEnd,
            persona,
            managedMembers
        );
    }

    private SessionInfo minimalSessionInfo(String hsidUuid, Instant sessionStart, Instant sessionEnd) {
        return new SessionInfo(
            null,
            hsidUuid,
            sessionStart,
            sessionEnd,
            "self",
            Map.of()
        );
    }

    private OidcUser createEnrichedUser(OidcUser oidcUser, SessionInfo sessionInfo) {
        Map<String, Object> additionalAttributes = Map.of("sessionInfo", sessionInfo);
        return new EnrichedOidcUser(
            oidcUser.getAuthorities(),
            oidcUser.getIdToken(),
            oidcUser.getUserInfo(),
            additionalAttributes
        );
    }
}
