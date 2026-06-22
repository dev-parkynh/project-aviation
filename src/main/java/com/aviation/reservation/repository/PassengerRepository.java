package com.aviation.reservation.repository;

import com.aviation.reservation.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    Optional<Passenger> findByEmail(String email);

    Optional<Passenger> findByPassportNumber(String passportNumber);

    boolean existsByEmail(String email);
}
