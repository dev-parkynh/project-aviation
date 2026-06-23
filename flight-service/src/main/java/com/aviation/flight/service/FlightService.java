package com.aviation.flight.service;

import com.aviation.flight.dto.FlightDto;
import com.aviation.flight.entity.Flight;
import com.aviation.flight.repository.FlightRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional
    public FlightDto.Response updateFlight(Long id, FlightDto.UpdateRequest request) {
        Flight flight = findById(id);
        if (request.getFlightNumber() != null) flight.setFlightNumber(request.getFlightNumber());
        if (request.getOrigin() != null) flight.setOrigin(request.getOrigin());
        if (request.getDestination() != null) flight.setDestination(request.getDestination());
        if (request.getDepartureTime() != null) flight.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalTime() != null) flight.setArrivalTime(request.getArrivalTime());
        if (request.getPrice() != null) flight.setPrice(request.getPrice());
        if (request.getStatus() != null) flight.setStatus(request.getStatus());
        if (request.getTotalSeats() != null) {
            int diff = request.getTotalSeats() - flight.getTotalSeats();
            flight.setTotalSeats(request.getTotalSeats());
            flight.setAvailableSeats(Math.max(0, flight.getAvailableSeats() + diff));
        }
        return toResponse(flight);
    }

    public FlightDto.Response getFlight(Long id) {
        return toResponse(findById(id));
    }

    public List<FlightDto.Response> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FlightDto.Response> searchFlights(String origin, String destination, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return flightRepository.findAvailableFlights(origin, destination, start, end).stream()
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
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .price(flight.getPrice())
                .status(flight.getStatus())
                .build();
    }
}
