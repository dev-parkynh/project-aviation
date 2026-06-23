package com.aviation.flight.dto;

import com.aviation.flight.entity.Seat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InternalDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatReserveRequest {
        private Long flightId;
    }

    @Getter
    @Builder
    public static class SeatReserveResponse {
        private Long flightId;
        private String flightNumber;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private BigDecimal price;
        private Long seatId;
        private String seatNo;
        private Seat.SeatClass seatClass;
    }
}
