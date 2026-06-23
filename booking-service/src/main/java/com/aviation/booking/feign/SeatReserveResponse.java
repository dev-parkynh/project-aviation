package com.aviation.booking.feign;

import com.aviation.booking.entity.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SeatReserveResponse {
    private Long flightId;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private BigDecimal price;
    private Long seatId;
    private String seatNo;
    private Reservation.SeatClass seatClass;
}
