package com.aviation.reservation.repository;

import com.aviation.reservation.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findByReservationId(Long reservationId);
}
