package com.aviation.reservation.service;

import com.aviation.reservation.dto.PassengerDto;
import com.aviation.reservation.dto.ReservationDto;
import com.aviation.reservation.entity.*;
import com.aviation.reservation.repository.ReservationRepository;
import com.aviation.reservation.repository.SeatRepository;
import com.aviation.reservation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final FlightService flightService;
    private final MailService mailService;

    @Transactional
    public ReservationDto.Response createReservation(ReservationDto.Request request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        Flight flight = flightService.findById(request.getFlightId());

        // SELECT FOR UPDATE — 비관적 락으로 동시 예약 방지
        Seat seat = seatRepository.findByIdWithLock(request.getSeatId())
                .orElseThrow(() -> new EntityNotFoundException("Seat not found: " + request.getSeatId()));

        if (!seat.getFlight().getId().equals(flight.getId())) {
            throw new IllegalArgumentException("Seat does not belong to the specified flight");
        }
        if (!seat.getIsAvailable()) {
            throw new IllegalStateException("Seat is already booked: " + seat.getSeatNo());
        }
        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No available seats on flight: " + flight.getFlightNumber());
        }

        seat.setIsAvailable(false);
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);

        Reservation reservation = Reservation.builder()
                .user(user)
                .flight(flight)
                .seat(seat)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .totalPrice(flight.getPrice())
                .createdAt(LocalDateTime.now())
                .build();

        List<Passenger> passengers = request.getPassengers().stream()
                .map(p -> Passenger.builder()
                        .reservation(reservation)
                        .name(p.getName())
                        .passportNo(p.getPassportNo())
                        .nationality(p.getNationality())
                        .build())
                .collect(Collectors.toList());
        reservation.getPassengers().addAll(passengers);

        ReservationDto.Response response = toResponse(reservationRepository.save(reservation));
        mailService.sendReservationConfirm(user.getEmail(), response);
        return response;
    }

    public List<ReservationDto.Response> getMyReservations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return reservationRepository.findByUserEmail(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReservationDto.Response getReservation(Long id) {
        return toResponse(reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id)));
    }

    @Transactional
    public ReservationDto.Response cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation already cancelled");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.getSeat().setIsAvailable(true);
        reservation.getFlight().setAvailableSeats(reservation.getFlight().getAvailableSeats() + 1);

        return toResponse(reservation);
    }

    private ReservationDto.Response toResponse(Reservation reservation) {
        List<PassengerDto.Response> passengerResponses = reservation.getPassengers().stream()
                .map(p -> PassengerDto.Response.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .passportNo(p.getPassportNo())
                        .nationality(p.getNationality())
                        .build())
                .collect(Collectors.toList());

        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .userName(reservation.getUser().getName())
                .flightNumber(reservation.getFlight().getFlightNumber())
                .origin(reservation.getFlight().getOrigin())
                .destination(reservation.getFlight().getDestination())
                .departureTime(reservation.getFlight().getDepartureTime())
                .seatNo(reservation.getSeat().getSeatNo())
                .seatClass(reservation.getSeat().getSeatClass())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .createdAt(reservation.getCreatedAt())
                .passengers(passengerResponses)
                .build();
    }
}
