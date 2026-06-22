package com.aviation.reservation.service;

import com.aviation.reservation.dto.FlightDto;
import com.aviation.reservation.entity.Flight;
import com.aviation.reservation.repository.FlightRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService {

    private final FlightRepository flightRepository;

    @Transactional
    public FlightDto.Response createFlight(FlightDto.Request request) {
        if (flightRepository.findByFlightNumber(request.getFlightNumber()).isPresent()) {
            throw new IllegalArgumentException("Flight number already exists: " + request.getFlightNumber());
        }
        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .price(request.getPrice())
                .status(Flight.FlightStatus.SCHEDULED)
                .build();
        return toResponse(flightRepository.save(flight));
    }

    public FlightDto.Response getFlight(Long id) {
        return toResponse(findById(id));
    }

    public List<FlightDto.Response> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FlightDto.Response> searchFlights(String origin, String destination, LocalDateTime departureTime) {
        return flightRepository.findAvailableFlights(origin, destination, departureTime).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FlightDto.Response updateFlightStatus(Long id, Flight.FlightStatus status) {
        Flight flight = findById(id);
        flight.setStatus(status);
        return toResponse(flight);
    }

    public Flight findById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found: " + id));
    }

    private FlightDto.Response toResponse(Flight flight) {
        return FlightDto.Response.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .availableSeats(flight.getAvailableSeats())
                .price(flight.getPrice())
                .status(flight.getStatus())
                .build();
    }
}
