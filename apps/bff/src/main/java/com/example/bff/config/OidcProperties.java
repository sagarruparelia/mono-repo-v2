package com.example.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oidc")
public class OidcProperties {
    private String issuerUri;
    private String authorizationUri;
    private String tokenUri;
    private String userinfoUri;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scopes;
    private String frontendRedirectUri;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getAuthorizationUri() {
        return authorizationUri;
    }

    public void setAuthorizationUri(String authorizationUri) {
        this.authorizationUri = authorizationUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getUserinfoUri() {
        return userinfoUri;
    }

    public void setUserinfoUri(String userinfoUri) {
        this.userinfoUri = userinfoUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getFrontendRedirectUri() {
        return frontendRedirectUri;
    }

    public void setFrontendRedirectUri(String frontendRedirectUri) {
        this.frontendRedirectUri = frontendRedirectUri;
    }
}
