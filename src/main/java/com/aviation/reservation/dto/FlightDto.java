package com.aviation.reservation.dto;

import com.aviation.reservation.entity.Flight;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FlightDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank
        @Size(max = 10)
        private String flightNumber;

        @NotBlank
        private String origin;

        @NotBlank
        private String destination;

        @NotNull
        @Future
        private LocalDateTime departureTime;

        @NotNull
        @Future
        private LocalDateTime arrivalTime;

        @NotNull
        @Min(1)
        private Integer totalSeats;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal price;
    }

    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String flightNumber;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private Integer availableSeats;
        private BigDecimal price;
        private Flight.FlightStatus status;
    }
}
