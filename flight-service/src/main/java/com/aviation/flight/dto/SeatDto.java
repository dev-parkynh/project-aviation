package com.aviation.flight.dto;

import com.aviation.flight.entity.Seat;
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
