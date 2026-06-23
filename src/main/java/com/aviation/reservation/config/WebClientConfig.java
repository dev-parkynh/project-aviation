package com.aviation.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("weatherWebClient")
    public WebClient weatherWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openweathermap.org")
                .build();
    }

    @Bean("exchangeRateWebClient")
    public WebClient exchangeRateWebClient() {
        return WebClient.builder()
                .baseUrl("https://open.er-api.com")
                .build();
    }
}
