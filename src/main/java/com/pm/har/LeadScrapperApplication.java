package com.pm.har;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@SpringBootApplication
@EnableOAuth2Client
@EnableScheduling
public class LeadScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeadScrapperApplication.class, args);
    }
}
