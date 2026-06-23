package com.aviation.flight.service;

import com.aviation.flight.dto.ExternalApiDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExternalApiService {

    private final WebClient weatherWebClient;
    private final WebClient exchangeRateWebClient;

    @Value("${weather.api.key}")
    private String weatherApiKey;

    public ExternalApiService(
            @Qualifier("weatherWebClient") WebClient weatherWebClient,
            @Qualifier("exchangeRateWebClient") WebClient exchangeRateWebClient) {
        this.weatherWebClient = weatherWebClient;
        this.exchangeRateWebClient = exchangeRateWebClient;
    }

    public ExternalApiDto.WeatherResponse getWeatherByCity(String city) {
        log.debug("날씨 조회 요청: {}", city);
        Map<?, ?> raw = weatherWebClient.get()
                .uri(uri -> uri
                        .path("/data/2.5/weather")
                        .queryParam("q", city)
                        .queryParam("appid", weatherApiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "kr")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<?, ?> main = (Map<?, ?>) raw.get("main");
        List<?> weatherList = (List<?>) raw.get("weather");
        Map<?, ?> wind = (Map<?, ?>) raw.get("wind");
        Map<?, ?> desc = (Map<?, ?>) weatherList.get(0);

        return ExternalApiDto.WeatherResponse.builder()
                .city((String) raw.get("name"))
                .temperature(((Number) main.get("temp")).doubleValue())
                .feelsLike(((Number) main.get("feels_like")).doubleValue())
                .humidity(((Number) main.get("humidity")).intValue())
                .description((String) desc.get("description"))
                .windSpeed(((Number) wind.get("speed")).doubleValue())
                .build();
    }

    @Cacheable("exchangeRates")
    public ExternalApiDto.ExchangeRateResponse getExchangeRates() {
        log.debug("환율 조회 요청 (USD 기준)");
        Map<?, ?> raw = exchangeRateWebClient.get()
                .uri("/v6/latest/USD")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<?, ?> rawRates = (Map<?, ?>) raw.get("rates");
        Map<String, Double> rates = rawRates.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> ((Number) e.getValue()).doubleValue()
                ));

        return ExternalApiDto.ExchangeRateResponse.builder()
                .base("USD")
                .rates(rates)
                .updatedAt(String.valueOf(raw.get("time_last_update_utc")))
                .build();
    }
}
