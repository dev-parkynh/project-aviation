package com.aviation.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

public class ExternalApiDto {

    @Getter
    @Builder
    public static class WeatherResponse {
        private String city;
        private double temperature;
        private double feelsLike;
        private int humidity;
        private String description;
        private double windSpeed;
    }

    @Getter
    @Builder
    public static class ExchangeRateResponse {
        private String base;
        private Map<String, Double> rates;
        private String updatedAt;
    }
}
