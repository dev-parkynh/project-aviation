package com.aviation.booking.service;

import com.aviation.booking.dto.PassengerDto;
import com.aviation.booking.dto.ReservationDto;
import com.aviation.booking.entity.Passenger;
import com.aviation.booking.entity.Reservation;
import com.aviation.booking.feign.FlightServiceClient;
import com.aviation.booking.feign.SeatReserveRequest;
import com.aviation.booking.feign.SeatReserveResponse;
import com.aviation.booking.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final FlightServiceClient flightServiceClient;
    private final MailService mailService;

    @Transactional
    public ReservationDto.Response createReservation(ReservationDto.Request request) {
        var auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        String userName = (String) auth.getDetails();

        SeatReserveResponse snapshot = flightServiceClient.reserveSeat(
                request.getSeatId(),
                new SeatReserveRequest(request.getFlightId())
        );

        Reservation reservation = Reservation.builder()
                .userEmail(email)
                .userName(userName != null ? userName : email)
                .flightId(snapshot.getFlightId())
                .flightNumber(snapshot.getFlightNumber())
                .origin(snapshot.getOrigin())
                .destination(snapshot.getDestination())
                .departureTime(snapshot.getDepartureTime())
                .seatId(snapshot.getSeatId())
                .seatNo(snapshot.getSeatNo())
                .seatClass(snapshot.getSeatClass())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .totalPrice(snapshot.getPrice())
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
        mailService.sendReservationConfirm(email, response);
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

        flightServiceClient.releaseSeat(
                reservation.getSeatId(),
                new SeatReserveRequest(reservation.getFlightId())
        );

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
                .userName(reservation.getUserName())
                .flightNumber(reservation.getFlightNumber())
                .origin(reservation.getOrigin())
                .destination(reservation.getDestination())
                .departureTime(reservation.getDepartureTime())
                .seatNo(reservation.getSeatNo())
                .seatClass(reservation.getSeatClass())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .createdAt(reservation.getCreatedAt())
                .passengers(passengerResponses)
                .build();
    }
}
