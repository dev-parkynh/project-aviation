package com.aviation.flight.controller;

import com.aviation.flight.dto.InternalDto;
import com.aviation.flight.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/seats")
@RequiredArgsConstructor
public class InternalFlightController {

    private final SeatService seatService;

    @PostMapping("/{seatId}/reserve")
    public ResponseEntity<InternalDto.SeatReserveResponse> reserveSeat(
            @PathVariable Long seatId,
            @RequestBody InternalDto.SeatReserveRequest request) {
        return ResponseEntity.ok(seatService.reserveSeat(seatId, request.getFlightId()));
    }

    @PostMapping("/{seatId}/release")
    public ResponseEntity<Void> releaseSeat(
            @PathVariable Long seatId,
            @RequestBody InternalDto.SeatReserveRequest request) {
        seatService.releaseSeat(seatId, request.getFlightId());
        return ResponseEntity.ok().build();
    }
}
