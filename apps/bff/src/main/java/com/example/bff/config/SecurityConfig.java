package com.example.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.frontend-redirect-path}")
    private String frontendRedirectPath;

    @Value("${app.frontend-error-path}")
    private String frontendErrorPath;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository) {

        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/auth/session").permitAll()
                .pathMatchers("/api/auth/login").permitAll()
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler(authenticationSuccessHandler())
                .authenticationFailureHandler(authenticationFailureHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
            )
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .build();
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    private ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        return (webFilterExchange, authentication) -> {
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
            webFilterExchange.getExchange().getResponse().getHeaders()
                .setLocation(URI.create(frontendRedirectPath));
            return webFilterExchange.getExchange().getResponse().setComplete();
        };
    }

    private ServerAuthenticationFailureHandler authenticationFailureHandler() {
        return (webFilterExchange, exception) -> {
            String errorUrl = frontendErrorPath + "?error=" +
                URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
            webFilterExchange.getExchange().getResponse().getHeaders()
                .setLocation(URI.create(errorUrl));
            return webFilterExchange.getExchange().getResponse().setComplete();
        };
    }

    private ServerLogoutSuccessHandler logoutSuccessHandler() {
        return (exchange, authentication) -> {
            exchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getExchange().getResponse().setComplete();
        };
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
