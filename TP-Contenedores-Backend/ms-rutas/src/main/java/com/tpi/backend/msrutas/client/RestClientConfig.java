package com.tpi.backend.msrutas.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient flotaRestClient(@Value("${ms.flota.url}") String flotaBaseUrl) {
        return RestClient.builder()
                .baseUrl(flotaBaseUrl)
                .build();
    }
}