package com.pm.har;

import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application
                .bannerMode(Banner.Mode.OFF)
                .properties("spring.config.location=file:${catalina.base}/conf/har.properties")
                .sources(LeadScrapperApplication.class);
    }


}
