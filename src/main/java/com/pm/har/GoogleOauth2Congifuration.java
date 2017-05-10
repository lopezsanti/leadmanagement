package com.pm.har;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.GmailScopes;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GoogleOauth2Congifuration extends WebSecurityConfigurerAdapter {

    @Value("${app.tokens.file}")
    private String tokensDbPath;
    @Autowired
    @Qualifier("oauth2ClientContextFilter")
    private Filter oauth2ClientContextFilter;

    @Bean(name = "googleResource")
    public OAuth2ProtectedResourceDetails googleResource() throws IOException {
        // load client secrets
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleClientSecrets secrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(getClass().getResourceAsStream("/client_secrets.json")));
        GoogleClientSecrets.Details web = secrets.getWeb();

        AuthorizationCodeResourceDetails details = new RefreshableResourceDetails();
        details.setId("google");
        details.setAccessTokenUri(web.getTokenUri());
        details.setClientId(web.getClientId());
        details.setClientSecret(web.getClientSecret());
        details.setUserAuthorizationUri(web.getAuthUri() + "?access_type=offline");
        details.setScope(Collections.singletonList(GmailScopes.GMAIL_MODIFY));
        return details;
    }

    @Bean(name = "googleOperations")
    public OAuth2RestOperations restTemplate(OAuth2ClientContext oauth2ClientContext) throws IOException {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(googleResource(), oauth2ClientContext);
        AccessTokenProvider provider = getAccessTokenProvider();
        restTemplate.setAccessTokenProvider(provider);
        return restTemplate;
    }

    @Bean(name = "googleAccessTokenProvider")
    @NotNull
    public AccessTokenProvider getAccessTokenProvider() {
        AccessTokenProviderChain provider = new AccessTokenProviderChain(Collections.singletonList(new AuthorizationCodeAccessTokenProvider()));
        provider.setClientTokenServices(clientTokenServices());
        return provider;
    }

    @Bean(name = "offlineOperations")
    public OAuth2RestOperations restTemplateOffline() throws IOException {
        OAuth2ClientContext oauth2ClientContext = new DefaultOAuth2ClientContext();
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(googleResource(), oauth2ClientContext);
        AccessTokenProvider provider = getAccessTokenProviderOffline();
        restTemplate.setAccessTokenProvider(provider);
        return restTemplate;
    }

    @Bean(name = "offlineAccessTokenProvider")
    @NotNull
    public AccessTokenProvider getAccessTokenProviderOffline() {
        AccessTokenProviderChain provider = new AccessTokenProviderChain(Collections.singletonList(new StoredAccessTokenProvider(clientTokenServices())));
        provider.setClientTokenServices(clientTokenServices());
        return provider;
    }

    @Bean(name = "commonTokens")
    public ClientTokenServices clientTokenServices() {
        return new FileMapDbSingleClientTokenService(tokensDbPath);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.addFilterAfter(oauth2ClientContextFilter, BasicAuthenticationFilter.class);
    }

    private static class RefreshableResourceDetails extends AuthorizationCodeResourceDetails {
        @Override
        public boolean isClientOnly() {
            return true;
        }
    }
}
