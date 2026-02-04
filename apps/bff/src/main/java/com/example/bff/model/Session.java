package com.example.bff.model;

import java.time.Instant;

public class Session {
    private final String id;
    private final String accessToken;
    private final String refreshToken;
    private final String idToken;
    private final Instant expiresAt;
    private final UserInfo userInfo;

    public Session(String id, String accessToken, String refreshToken, String idToken, Instant expiresAt, UserInfo userInfo) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.idToken = idToken;
        this.expiresAt = expiresAt;
        this.userInfo = userInfo;
    }

    public String getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
