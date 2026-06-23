package com.aviation.reservation.repository;

import com.aviation.reservation.entity.MonthlyStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyStatRepository extends JpaRepository<MonthlyStat, Long> {

    Optional<MonthlyStat> findByStatYearAndStatMonthAndOriginAndDestination(
            int statYear, int statMonth, String origin, String destination);
}
