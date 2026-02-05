package com.example.bff.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OidcCallbackRedirectFilter} validating reactive behavior
 * and correct routing of OIDC callbacks.
 */
class OidcCallbackRedirectFilterTest {

    private OidcCallbackRedirectFilter filter;
    private WebFilterChain mockChain;

    @BeforeEach
    void setUp() throws Exception {
        filter = new OidcCallbackRedirectFilter();

        // Set the frontend redirect path via reflection
        Field field = OidcCallbackRedirectFilter.class.getDeclaredField("frontendRedirectPath");
        field.setAccessible(true);
        field.set(filter, "http://localhost:4200/dashboard");

        mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());
    }

    @Nested
    @DisplayName("OIDC Callback Detection (code & state params)")
    class OidcCallbackTests {

        @Test
        @DisplayName("Root URL with code & state → redirects to /login/oauth2/code/hsid")
        void rootWithCodeAndState_redirectsToCallback() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/?code=AUTH_CODE&state=STATE_VALUE")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            // Verify reactive completion
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify redirect
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
            URI location = exchange.getResponse().getHeaders().getLocation();
            assertThat(location).isNotNull();
            assertThat(location.getPath()).isEqualTo("/login/oauth2/code/hsid");
            assertThat(location.getQuery()).contains("code=AUTH_CODE");
            assertThat(location.getQuery()).contains("state=STATE_VALUE");
        }

        @Test
        @DisplayName("Any path with code & state → redirects to /login/oauth2/code/hsid")
        void anyPathWithCodeAndState_redirectsToCallback() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/some/frontend/path?code=ABC&state=XYZ")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
            URI location = exchange.getResponse().getHeaders().getLocation();
            assertThat(location.getPath()).isEqualTo("/login/oauth2/code/hsid");
        }

        @Test
        @DisplayName("Already at callback path with code & state → passes through")
        void callbackPathWithCodeAndState_passesThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/login/oauth2/code/hsid?code=ABC&state=XYZ")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            // Should NOT set redirect - passes through to chain
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        @Test
        @DisplayName("Only code param (no state) → treated as regular request")
        void onlyCodeParam_treatedAsRegularRequest() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/?code=ABC")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            // Should redirect to frontend (not OIDC callback)
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
            URI location = exchange.getResponse().getHeaders().getLocation();
            assertThat(location.toString()).isEqualTo("http://localhost:4200/dashboard");
        }

        @Test
        @DisplayName("Only state param (no code) → treated as regular request")
        void onlyStateParam_treatedAsRegularRequest() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/?state=XYZ")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            // Should redirect to frontend
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        }
    }

    @Nested
    @DisplayName("Backend Path Detection")
    class BackendPathTests {

        @Test
        @DisplayName("/api/** → passes through to backend")
        void apiPath_passesThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/users")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            // No redirect set - passes through
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        @Test
        @DisplayName("/actuator/** → passes through to backend")
        void actuatorPath_passesThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/actuator/health")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        @Test
        @DisplayName("/oauth2/** → passes through to Spring Security")
        void oauth2Path_passesThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/oauth2/authorization/hsid")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Frontend Redirect")
    class FrontendRedirectTests {

        @Test
        @DisplayName("Root path → redirects to frontend")
        void rootPath_redirectsToFrontend() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
            URI location = exchange.getResponse().getHeaders().getLocation();
            assertThat(location.toString()).isEqualTo("http://localhost:4200/dashboard");
        }

        @Test
        @DisplayName("Unknown path → redirects to frontend")
        void unknownPath_redirectsToFrontend() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/some/random/path")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            StepVerifier.create(result)
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        }
    }

    @Nested
    @DisplayName("Reactive Behavior Validation")
    class ReactiveBehaviorTests {

        @Test
        @DisplayName("Filter returns Mono<Void> - non-blocking")
        void filterReturnsMonoVoid() {
            MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            Mono<Void> result = filter.filter(exchange, mockChain);

            // Verify it's a Mono that completes without blocking
            assertThat(result).isNotNull();
            StepVerifier.create(result)
                    .expectSubscription()
                    .verifyComplete();
        }

        @Test
        @DisplayName("Multiple concurrent requests - non-blocking")
        void multipleConcurrentRequests_nonBlocking() {
            // Create multiple exchanges
            MockServerWebExchange exchange1 = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/?code=A&state=1").build());
            MockServerWebExchange exchange2 = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/?code=B&state=2").build());
            MockServerWebExchange exchange3 = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/test").build());

            // Execute concurrently
            Mono<Void> combined = Mono.when(
                    filter.filter(exchange1, mockChain),
                    filter.filter(exchange2, mockChain),
                    filter.filter(exchange3, mockChain)
            );

            StepVerifier.create(combined)
                    .verifyComplete();

            // Verify correct handling
            assertThat(exchange1.getResponse().getHeaders().getLocation().getQuery())
                    .contains("code=A");
            assertThat(exchange2.getResponse().getHeaders().getLocation().getQuery())
                    .contains("code=B");
            assertThat(exchange3.getResponse().getStatusCode()).isNull(); // passed through
        }
    }
}
