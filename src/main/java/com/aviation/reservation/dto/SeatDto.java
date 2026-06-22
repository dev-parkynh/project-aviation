package com.aviation.reservation.dto;

import com.aviation.reservation.entity.Seat;
import lombok.Builder;
import lombok.Getter;

public class SeatDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String seatNo;
        private Seat.SeatClass seatClass;
        private Boolean isAvailable;
    }
}
