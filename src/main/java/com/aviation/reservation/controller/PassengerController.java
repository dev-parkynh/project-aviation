package com.aviation.reservation.controller;

import com.aviation.reservation.dto.PassengerDto;
import com.aviation.reservation.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<PassengerDto.Response> registerPassenger(@Valid @RequestBody PassengerDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.registerPassenger(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDto.Response> getPassenger(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.getPassenger(id));
    }

    @GetMapping
    public ResponseEntity<List<PassengerDto.Response>> getAllPassengers() {
        return ResponseEntity.ok(passengerService.getAllPassengers());
    }
}
