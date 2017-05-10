package com.pm.har;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.ClientKeyGenerator;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.client.token.DefaultClientKeyGenerator;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MemoryClientTokenServices implements ClientTokenServices {
    private ClientKeyGenerator keyGenerator = new DefaultClientKeyGenerator();

    private ConcurrentMap<String, OAuth2AccessToken> tokensByPrincipal = new ConcurrentHashMap<>();

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String key = keyGenerator.extractKey(resource, authentication);
        return tokensByPrincipal.get(key);
    }

    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication, OAuth2AccessToken accessToken) {
        String key = keyGenerator.extractKey(resource, authentication);
        tokensByPrincipal.put(key, accessToken);
    }

    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String key = keyGenerator.extractKey(resource, authentication);
        tokensByPrincipal.remove(key);
    }
}
