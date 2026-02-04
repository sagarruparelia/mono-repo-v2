package com.example.bff.security;

/**
 * Constants for persona values used with {@link RequiredPersona} annotation.
 * <p>
 * Usage:
 * <pre>
 * {@literal @}RequiredPersona(Persona.SELF)
 * {@literal @}RequiredPersona(Persona.REPRESENTATIVE)
 * {@literal @}RequiredPersona({Persona.SELF, Persona.REPRESENTATIVE})
 * </pre>
 */
public final class Persona {

    public static final String SELF = "self";
    public static final String REPRESENTATIVE = "representative";

    private Persona() {
        // Prevent instantiation
    }
}
