package com.example.bff.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;

/**
 * Exception thrown when a user's persona does not match the required persona(s)
 * for an endpoint annotated with {@link RequiredPersona}.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PersonaAccessDeniedException extends RuntimeException {

    private final String[] requiredPersonas;
    private final String actualPersona;

    public PersonaAccessDeniedException(String[] requiredPersonas, String actualPersona) {
        super(buildMessage(requiredPersonas, actualPersona));
        this.requiredPersonas = requiredPersonas;
        this.actualPersona = actualPersona;
    }

    private static String buildMessage(String[] requiredPersonas, String actualPersona) {
        return "Access denied. Required persona: %s, actual persona: %s".formatted(
                Arrays.toString(requiredPersonas),
                actualPersona != null ? actualPersona : "none"
        );
    }

    public String[] getRequiredPersonas() {
        return requiredPersonas;
    }

    public String getActualPersona() {
        return actualPersona;
    }
}
