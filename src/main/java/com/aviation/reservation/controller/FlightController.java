package com.aviation.reservation.controller;

import com.aviation.reservation.dto.ExternalApiDto;
import com.aviation.reservation.dto.FlightDto;
import com.aviation.reservation.dto.SeatDto;
import com.aviation.reservation.entity.Flight;
import com.aviation.reservation.service.ExternalApiService;
import com.aviation.reservation.service.FlightService;
import com.aviation.reservation.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final SeatService seatService;
    private final ExternalApiService externalApiService;

    @GetMapping
    public ResponseEntity<List<FlightDto.Response>> getFlights(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String arrival,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (departure != null && arrival != null && date != null) {
            return ResponseEntity.ok(flightService.searchFlights(departure, arrival, date));
        }
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDto.Response> getFlight(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlight(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDto.Response>> getSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByFlightId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FlightDto.Response> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam Flight.FlightStatus status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, status));
    }

    @GetMapping("/{id}/weather")
    public ResponseEntity<ExternalApiDto.WeatherResponse> getFlightWeather(@PathVariable Long id) {
        String destination = flightService.getFlight(id).getDestination();
        return ResponseEntity.ok(externalApiService.getWeatherByCity(destination));
    }

    @GetMapping("/exchange-rate")
    public ResponseEntity<ExternalApiDto.ExchangeRateResponse> getExchangeRate() {
        return ResponseEntity.ok(externalApiService.getExchangeRates());
    }
}
