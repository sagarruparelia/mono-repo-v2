package com.example.bff.service;

import com.example.bff.model.Session;
import com.example.bff.model.TokenResponse;
import com.example.bff.model.UserInfo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> pkceStore = new ConcurrentHashMap<>();

    public Mono<Session> createSession(TokenResponse tokens) {
        return Mono.fromCallable(() -> {
            String sessionId = UUID.randomUUID().toString();
            UserInfo userInfo = extractUserInfo(tokens.idToken());
            Session session = new Session(
                    sessionId,
                    tokens.accessToken(),
                    tokens.refreshToken(),
                    tokens.idToken(),
                    Instant.now().plusSeconds(tokens.expiresIn()),
                    userInfo
            );
            sessions.put(sessionId, session);
            return session;
        });
    }

    public Mono<Session> validateSession(String sessionId) {
        return Mono.fromCallable(() -> {
            Session session = sessions.get(sessionId);
            if (session == null || session.isExpired()) {
                if (session != null) {
                    sessions.remove(sessionId);
                }
                return null;
            }
            return session;
        });
    }

    public Mono<Void> destroySession(String sessionId) {
        return Mono.fromRunnable(() -> sessions.remove(sessionId));
    }

    public void storePkceVerifier(String state, String codeVerifier) {
        pkceStore.put(state, codeVerifier);
    }

    public String getPkceVerifier(String state) {
        return pkceStore.remove(state);
    }

    private UserInfo extractUserInfo(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                return new UserInfo("unknown", "unknown@example.com", "Unknown User");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            // Simple JSON parsing for demo - in production use a proper JSON library
            String sub = extractClaim(payload, "sub");
            String email = extractClaim(payload, "email");
            String name = extractClaim(payload, "name");
            return new UserInfo(
                    sub != null ? sub : "unknown",
                    email != null ? email : "unknown@example.com",
                    name != null ? name : "Unknown User"
            );
        } catch (Exception e) {
            return new UserInfo("unknown", "unknown@example.com", "Unknown User");
        }
    }

    private String extractClaim(String json, String claim) {
        String searchKey = "\"" + claim + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return json.substring(startIndex, endIndex);
    }
}
