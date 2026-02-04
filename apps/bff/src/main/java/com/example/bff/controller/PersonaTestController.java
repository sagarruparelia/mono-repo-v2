package com.example.bff.controller;

import com.example.bff.security.EnrichedOidcUser;
import com.example.bff.security.Persona;
import com.example.bff.security.RequiredPersona;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Test controller demonstrating the {@link RequiredPersona} annotation.
 */
@RestController
@RequestMapping("/api/persona-test")
public class PersonaTestController {

    @GetMapping("/self-only")
    @RequiredPersona(Persona.SELF)
    public Mono<PersonaResponse> selfOnly(@AuthenticationPrincipal OidcUser oidcUser) {
        String persona = extractPersona(oidcUser);
        return Mono.just(new PersonaResponse(
                "/api/persona-test/self-only",
                persona,
                "Access granted for self persona"
        ));
    }

    @GetMapping("/representative-only")
    @RequiredPersona(Persona.REPRESENTATIVE)
    public Mono<PersonaResponse> representativeOnly(@AuthenticationPrincipal OidcUser oidcUser) {
        String persona = extractPersona(oidcUser);
        return Mono.just(new PersonaResponse(
                "/api/persona-test/representative-only",
                persona,
                "Access granted for representative persona"
        ));
    }

    @GetMapping("/any-persona")
    @RequiredPersona({Persona.SELF, Persona.REPRESENTATIVE})
    public Mono<PersonaResponse> anyPersona(@AuthenticationPrincipal OidcUser oidcUser) {
        String persona = extractPersona(oidcUser);
        return Mono.just(new PersonaResponse(
                "/api/persona-test/any-persona",
                persona,
                "Access granted for any persona"
        ));
    }

    private String extractPersona(OidcUser oidcUser) {
        if (oidcUser instanceof EnrichedOidcUser enrichedUser) {
            var sessionInfo = enrichedUser.getSessionInfo();
            return sessionInfo != null ? sessionInfo.persona() : null;
        }
        return null;
    }

    public record PersonaResponse(String endpoint, String userPersona, String message) {}
}
