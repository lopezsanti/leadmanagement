package com.pm.har;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class StoredAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
    private ClientTokenServices clientTokenServices;

    public StoredAccessTokenProvider(ClientTokenServices clientTokenServices) {
        this.clientTokenServices = clientTokenServices;
    }

    @Override
    public OAuth2AccessToken obtainAccessToken(OAuth2ProtectedResourceDetails details, AccessTokenRequest parameters) throws UserRedirectRequiredException, UserApprovalRequiredException, AccessDeniedException {
        return clientTokenServices.getAccessToken(details, null);
    }
}
