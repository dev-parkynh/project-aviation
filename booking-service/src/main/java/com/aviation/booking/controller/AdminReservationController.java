package com.aviation.booking.controller;

import com.aviation.booking.dto.ReservationDto;
import com.aviation.booking.dto.StatsDto;
import com.aviation.booking.entity.Reservation;
import com.aviation.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminReservationController {

    private final AdminService adminService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto.Response>> getAllReservations() {
        return ResponseEntity.ok(adminService.getAllReservations());
    }

    @PatchMapping("/reservations/{id}/status")
    public ResponseEntity<ReservationDto.Response> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam Reservation.ReservationStatus status) {
        return ResponseEntity.ok(adminService.updateReservationStatus(id, status));
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<List<StatsDto.DailyStats>> getDailyStats() {
        return ResponseEntity.ok(adminService.getDailyStats());
    }

    @GetMapping("/stats/monthly")
    public ResponseEntity<List<StatsDto.MonthlyStats>> getMonthlyStats() {
        return ResponseEntity.ok(adminService.getMonthlyStats());
    }

    @GetMapping("/stats/route")
    public ResponseEntity<List<StatsDto.RouteStats>> getRouteStats() {
        return ResponseEntity.ok(adminService.getRouteStats());
    }
}
