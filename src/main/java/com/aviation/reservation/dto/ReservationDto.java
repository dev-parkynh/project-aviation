package com.aviation.reservation.dto;

import com.aviation.reservation.entity.Reservation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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
        private Long passengerId;

        @NotBlank
        private String seatNumber;
    }

    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String reservationCode;
        private String flightNumber;
        private String passengerName;
        private String seatNumber;
        private Reservation.ReservationStatus status;
        private LocalDateTime reservedAt;
    }
}
