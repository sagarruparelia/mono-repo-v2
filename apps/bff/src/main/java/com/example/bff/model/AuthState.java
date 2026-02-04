package com.example.bff.model;

public record AuthState(String state, String codeVerifier, String authorizationUrl) {
}
