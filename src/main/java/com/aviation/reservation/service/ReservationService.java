package com.aviation.reservation.service;

import com.aviation.reservation.dto.ReservationDto;
import com.aviation.reservation.entity.Flight;
import com.aviation.reservation.entity.Passenger;
import com.aviation.reservation.entity.Reservation;
import com.aviation.reservation.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final FlightService flightService;
    private final PassengerService passengerService;

    @Transactional
    public ReservationDto.Response createReservation(ReservationDto.Request request) {
        Flight flight = flightService.findById(request.getFlightId());
        Passenger passenger = passengerService.findById(request.getPassengerId());

        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No available seats on flight: " + flight.getFlightNumber());
        }
        if (reservationRepository.existsByFlightIdAndSeatNumber(flight.getId(), request.getSeatNumber())) {
            throw new IllegalArgumentException("Seat already taken: " + request.getSeatNumber());
        }

        Reservation reservation = Reservation.builder()
                .reservationCode(generateReservationCode())
                .flight(flight)
                .passenger(passenger)
                .seatNumber(request.getSeatNumber())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .build();

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationDto.Response getReservation(String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + reservationCode));
        return toResponse(reservation);
    }

    public List<ReservationDto.Response> getReservationsByPassenger(Long passengerId) {
        return reservationRepository.findByPassengerId(passengerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationDto.Response cancelReservation(String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + reservationCode));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation already cancelled");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.getFlight().setAvailableSeats(reservation.getFlight().getAvailableSeats() + 1);
        return toResponse(reservation);
    }

    private String generateReservationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ReservationDto.Response toResponse(Reservation reservation) {
        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .flightNumber(reservation.getFlight().getFlightNumber())
                .passengerName(reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName())
                .seatNumber(reservation.getSeatNumber())
                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())
                .build();
    }
}
