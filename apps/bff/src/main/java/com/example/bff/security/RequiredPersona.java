package com.example.bff.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts endpoint access based on the user's persona stored in SessionInfo.
 * <p>
 * Usage examples:
 * <pre>
 * {@literal @}RequiredPersona(Persona.SELF)                           // Only self persona
 * {@literal @}RequiredPersona(Persona.REPRESENTATIVE)                 // Only representative persona
 * {@literal @}RequiredPersona({Persona.SELF, Persona.REPRESENTATIVE}) // Either persona allowed
 * </pre>
 *
 * @see Persona
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredPersona {
    /**
     * The allowed persona values. Access is granted if the user's persona
     * matches any of the specified values.
     */
    String[] value();
}
