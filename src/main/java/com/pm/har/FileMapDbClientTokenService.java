package com.pm.har;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.ClientKeyGenerator;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.client.token.DefaultClientKeyGenerator;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.concurrent.ConcurrentMap;

public class FileMapDbClientTokenService implements ClientTokenServices {
    private final DB db;
    private final ConcurrentMap<String, OAuth2AccessToken> tokensByPrincipal;
    private ClientKeyGenerator keyGenerator = new DefaultClientKeyGenerator();

    public FileMapDbClientTokenService(String fileName) {
        db = DBMaker.fileDB(new File(fileName))
                .transactionEnable()
                .make();
        //noinspection unchecked
        tokensByPrincipal = db.hashMap("oauth2")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String key = keyGenerator.extractKey(resource, authentication);
        return tokensByPrincipal.get(key);
    }

    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication, OAuth2AccessToken accessToken) {
        String key = keyGenerator.extractKey(resource, authentication);
        tokensByPrincipal.put(key, accessToken);
        db.commit();
    }

    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String key = keyGenerator.extractKey(resource, authentication);
        tokensByPrincipal.remove(key);
        db.commit();
    }

    @PreDestroy
    public void shutdown() {
        db.close();
    }
}
