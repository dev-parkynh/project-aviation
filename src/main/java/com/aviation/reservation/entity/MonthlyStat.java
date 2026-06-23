package com.aviation.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_stats",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_monthly_stats_key",
                columnNames = {"stat_year", "stat_month", "origin", "destination"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_year", nullable = false)
    private int statYear;

    @Column(name = "stat_month", nullable = false)
    private int statMonth;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(nullable = false)
    private long reservationCount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;
}
