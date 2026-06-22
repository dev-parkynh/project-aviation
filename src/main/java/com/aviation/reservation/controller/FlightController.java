package com.aviation.reservation.controller;

import com.aviation.reservation.dto.FlightDto;
import com.aviation.reservation.entity.Flight;
import com.aviation.reservation.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<FlightDto.Response> createFlight(@Valid @RequestBody FlightDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDto.Response> getFlight(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlight(id));
    }

    @GetMapping
    public ResponseEntity<List<FlightDto.Response>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FlightDto.Response>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime) {
        return ResponseEntity.ok(flightService.searchFlights(origin, destination, departureTime));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FlightDto.Response> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam Flight.FlightStatus status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, status));
    }
}
