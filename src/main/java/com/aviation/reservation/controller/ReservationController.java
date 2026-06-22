package com.aviation.reservation.controller;

import com.aviation.reservation.dto.ReservationDto;
import com.aviation.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(@Valid @RequestBody ReservationDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto.Response>> getMyReservations() {
        return ResponseEntity.ok(reservationService.getMyReservations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto.Response> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationDto.Response> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }
}
