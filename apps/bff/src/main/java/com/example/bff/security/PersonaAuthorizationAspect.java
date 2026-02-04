package com.example.bff.security;

import com.example.bff.model.SessionInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Aspect that enforces persona-based authorization for methods annotated with {@link RequiredPersona}.
 * <p>
 * This aspect intercepts method calls and validates that the authenticated user's persona
 * (from {@link SessionInfo}) matches one of the allowed personas specified in the annotation.
 * <p>
 * <b>How it works:</b>
 * <ol>
 *   <li>Intercepts methods annotated with {@code @RequiredPersona}</li>
 *   <li>Retrieves the {@link org.springframework.security.core.context.SecurityContext} from
 *       {@link ReactiveSecurityContextHolder}</li>
 *   <li>Extracts the persona from {@link EnrichedOidcUser#getSessionInfo()}</li>
 *   <li>Compares against allowed personas from the annotation</li>
 *   <li>Returns 403 Forbidden via {@link PersonaAccessDeniedException} if no match</li>
 * </ol>
 * <p>
 * <b>Supported return types:</b> {@link Mono} and {@link Flux} (standard for WebFlux controllers).
 * Non-reactive return types are wrapped in a Mono for proper authorization handling.
 */
@Aspect
@Component
public class PersonaAuthorizationAspect {

    @Around("@annotation(requiredPersona)")
    public Object checkPersona(ProceedingJoinPoint joinPoint, RequiredPersona requiredPersona) throws Throwable {
        String[] allowedPersonas = requiredPersona.value();
        Set<String> allowedSet = Set.of(allowedPersonas);

        // Create authorization check that reads from reactive security context
        Mono<Void> authorizationCheck = ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new PersonaAccessDeniedException(allowedPersonas, null)))
                .flatMap(securityContext -> {
                    Object principal = securityContext.getAuthentication().getPrincipal();

                    if (principal instanceof EnrichedOidcUser enrichedUser) {
                        SessionInfo sessionInfo = enrichedUser.getSessionInfo();
                        String actualPersona = sessionInfo != null ? sessionInfo.persona() : null;

                        if (actualPersona != null && allowedSet.contains(actualPersona)) {
                            return Mono.empty(); // Authorization passed
                        }
                        return Mono.error(new PersonaAccessDeniedException(allowedPersonas, actualPersona));
                    }

                    return Mono.error(new PersonaAccessDeniedException(allowedPersonas, null));
                });

        // Check return type to determine handling strategy
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (Mono.class.isAssignableFrom(returnType)) {
            // For Mono: proceed() creates the Mono lazily, then chain with authorization
            Mono<?> methodMono = (Mono<?>) joinPoint.proceed();
            return authorizationCheck.then(methodMono);
        } else if (Flux.class.isAssignableFrom(returnType)) {
            // For Flux: proceed() creates the Flux lazily, then chain with authorization
            Flux<?> methodFlux = (Flux<?>) joinPoint.proceed();
            return authorizationCheck.thenMany(methodFlux);
        }

        // For non-reactive return types: defer method execution until after authorization
        // This ensures the method only runs if authorization passes
        return authorizationCheck
                .then(Mono.fromCallable(() -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }));
    }
}
