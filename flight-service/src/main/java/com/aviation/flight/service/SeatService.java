package com.aviation.flight.service;

import com.aviation.flight.dto.InternalDto;
import com.aviation.flight.dto.SeatDto;
import com.aviation.flight.entity.Flight;
import com.aviation.flight.entity.Seat;
import com.aviation.flight.repository.FlightRepository;
import com.aviation.flight.repository.SeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final FlightRepository flightRepository;

    public List<SeatDto.Response> getSeatsByFlightId(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new EntityNotFoundException("Flight not found: " + flightId);
        }
        return seatRepository.findByFlight_Id(flightId).stream()
                .map(seat -> SeatDto.Response.builder()
                        .id(seat.getId())
                        .seatNo(seat.getSeatNo())
                        .seatClass(seat.getSeatClass())
                        .isAvailable(seat.getIsAvailable())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public InternalDto.SeatReserveResponse reserveSeat(Long seatId, Long flightId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found: " + seatId));

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found: " + flightId));

        if (!seat.getFlight().getId().equals(flightId)) {
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

        return InternalDto.SeatReserveResponse.builder()
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .price(flight.getPrice())
                .seatId(seat.getId())
                .seatNo(seat.getSeatNo())
                .seatClass(seat.getSeatClass())
                .build();
    }

    @Transactional
    public void releaseSeat(Long seatId, Long flightId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found: " + seatId));

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found: " + flightId));

        seat.setIsAvailable(true);
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
    }
}
