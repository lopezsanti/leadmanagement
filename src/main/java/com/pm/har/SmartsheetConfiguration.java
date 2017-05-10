package com.pm.har;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.oauth.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmartsheetConfiguration {
    @Value("${app.smartsheet.api.token}")
    private String apiToken;

    @Bean
    public Smartsheet createSetvice() {
        // Set the Access Token
        Token token = new Token();
        token.setAccessToken(apiToken);

        // Use the Smartsheet Builder to create a Smartsheet
        return new SmartsheetBuilder()
                .setAccessToken(token.getAccessToken())
                .build();
    }

}
