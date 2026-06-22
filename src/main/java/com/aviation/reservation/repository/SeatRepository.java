package com.aviation.reservation.repository;

import com.aviation.reservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFlight_Id(Long flightId);

    List<Seat> findByFlight_IdAndIsAvailable(Long flightId, Boolean isAvailable);
}
