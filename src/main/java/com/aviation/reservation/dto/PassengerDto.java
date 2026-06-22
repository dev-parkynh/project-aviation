package com.aviation.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class PassengerDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank
        @Size(max = 100)
        private String name;

        @NotBlank
        @Size(max = 20)
        private String passportNo;

        @NotBlank
        @Size(max = 50)
        private String nationality;
    }

    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String name;
        private String passportNo;
        private String nationality;
    }
}
