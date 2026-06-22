package com.aviation.reservation.repository;

import com.aviation.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    List<Reservation> findByPassengerId(Long passengerId);

    List<Reservation> findByFlightId(Long flightId);

    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}
