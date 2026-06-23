package com.aviation.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "flight-service")
public interface FlightServiceClient {

    @PostMapping("/internal/seats/{seatId}/reserve")
    SeatReserveResponse reserveSeat(
            @PathVariable Long seatId,
            @RequestBody SeatReserveRequest request);

    @PostMapping("/internal/seats/{seatId}/release")
    void releaseSeat(
            @PathVariable Long seatId,
            @RequestBody SeatReserveRequest request);
}
