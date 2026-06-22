package com.aviation.reservation.repository;

import com.aviation.reservation.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    @Query("SELECT f FROM Flight f WHERE f.origin = :origin AND f.destination = :destination " +
           "AND f.departureTime >= :departureTime AND f.availableSeats > 0 " +
           "AND f.status = 'SCHEDULED'")
    List<Flight> findAvailableFlights(@Param("origin") String origin,
                                      @Param("destination") String destination,
                                      @Param("departureTime") LocalDateTime departureTime);

    List<Flight> findByStatus(Flight.FlightStatus status);
}
