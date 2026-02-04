package com.example.bff.security;

import com.example.bff.model.SessionInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;
import java.util.Map;

public class EnrichedOidcUser extends DefaultOidcUser {

    private final Map<String, Object> additionalAttributes;

    public EnrichedOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            OidcIdToken idToken,
            OidcUserInfo userInfo,
            Map<String, Object> additionalAttributes) {
        super(authorities, idToken, userInfo);
        this.additionalAttributes = additionalAttributes;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        if (additionalAttributes.containsKey(name)) {
            return (T) additionalAttributes.get(name);
        }
        return super.getAttribute(name);
    }

    public SessionInfo getSessionInfo() {
        return (SessionInfo) additionalAttributes.get("sessionInfo");
    }
}
