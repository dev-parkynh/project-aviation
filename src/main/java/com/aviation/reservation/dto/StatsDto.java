package com.aviation.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StatsDto {

    @Getter
    @Builder
    public static class DailyStats {
        private LocalDate date;
        private Long reservationCount;
        private BigDecimal totalRevenue;
    }

    @Getter
    @Builder
    public static class MonthlyStats {
        private Integer year;
        private Integer month;
        private Long reservationCount;
        private BigDecimal totalRevenue;
    }

    @Getter
    @Builder
    public static class RouteStats {
        private String origin;
        private String destination;
        private Long reservationCount;
        private BigDecimal totalRevenue;
    }
}
