package com.aviation.reservation.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class PassengerDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank
        @Size(max = 50)
        private String firstName;

        @NotBlank
        @Size(max = 50)
        private String lastName;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Pattern(regexp = "^[+]?[0-9]{10,15}$")
        private String phone;

        @NotBlank
        @Size(max = 20)
        private String passportNumber;
    }

    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String passportNumber;
    }
}
