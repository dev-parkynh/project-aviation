package com.aviation.reservation.service;

import com.aviation.reservation.dto.PassengerDto;
import com.aviation.reservation.entity.Passenger;
import com.aviation.reservation.repository.PassengerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassengerService {

    private final PassengerRepository passengerRepository;

    @Transactional
    public PassengerDto.Response registerPassenger(PassengerDto.Request request) {
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        Passenger passenger = Passenger.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passportNumber(request.getPassportNumber())
                .build();
        return toResponse(passengerRepository.save(passenger));
    }

    public PassengerDto.Response getPassenger(Long id) {
        return toResponse(findById(id));
    }

    public List<PassengerDto.Response> getAllPassengers() {
        return passengerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Passenger findById(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found: " + id));
    }

    private PassengerDto.Response toResponse(Passenger passenger) {
        return PassengerDto.Response.builder()
                .id(passenger.getId())
                .firstName(passenger.getFirstName())
                .lastName(passenger.getLastName())
                .email(passenger.getEmail())
                .phone(passenger.getPhone())
                .passportNumber(passenger.getPassportNumber())
                .build();
    }
}
