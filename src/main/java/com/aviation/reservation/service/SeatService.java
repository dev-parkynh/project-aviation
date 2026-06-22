package com.aviation.reservation.service;

import com.aviation.reservation.dto.SeatDto;
import com.aviation.reservation.repository.SeatRepository;
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
    private final FlightService flightService;

    public List<SeatDto.Response> getSeatsByFlightId(Long flightId) {
        flightService.findById(flightId);
        return seatRepository.findByFlight_Id(flightId).stream()
                .map(seat -> SeatDto.Response.builder()
                        .id(seat.getId())
                        .seatNo(seat.getSeatNo())
                        .seatClass(seat.getSeatClass())
                        .isAvailable(seat.getIsAvailable())
                        .build())
                .collect(Collectors.toList());
    }
}
