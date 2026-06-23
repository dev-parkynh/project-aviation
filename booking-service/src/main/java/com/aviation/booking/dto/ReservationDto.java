package com.aviation.booking.dto;

import com.aviation.booking.entity.Reservation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotNull
        private Long flightId;

        @NotNull
        private Long seatId;

        @NotEmpty
        @Valid
        private List<PassengerDto.Request> passengers;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String userName;
        private String flightNumber;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private String seatNo;
        private Reservation.SeatClass seatClass;
        private Reservation.ReservationStatus status;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
        private List<PassengerDto.Response> passengers;
    }
}
