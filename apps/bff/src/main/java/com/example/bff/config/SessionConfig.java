package com.example.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
public class SessionConfig {

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("SESSION_ID");
        resolver.addCookieInitializer(builder -> builder
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
        );
        return resolver;
    }
}
