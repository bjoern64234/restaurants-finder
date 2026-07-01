package org.example.backend.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient geoapifyRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.geoapify.com/v2")
                .build();
    }

    @Bean
    public RestClient autocompleteRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.geoapify.com/v1/geocode")
                .build();
    }
}
