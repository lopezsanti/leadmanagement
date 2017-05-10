package com.pm.har.rest;

import com.smartsheet.api.SmartsheetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
public class Login {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("googleOperations")
    private OAuth2RestOperations restTemplate;

    @RequestMapping("/")
    public String login() throws IOException, MessagingException, SmartsheetException {

        // initiate oauth2 flow
        OAuth2AccessToken accessToken = restTemplate.getAccessToken();

        return "ok";
    }
}
