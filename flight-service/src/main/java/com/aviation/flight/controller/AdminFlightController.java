package com.aviation.flight.controller;

import com.aviation.flight.dto.FlightDto;
import com.aviation.flight.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/flights")
@RequiredArgsConstructor
public class AdminFlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<FlightDto.Response> createFlight(@Valid @RequestBody FlightDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightDto.Response> updateFlight(
            @PathVariable Long id,
            @RequestBody FlightDto.UpdateRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(id, request));
    }
}
