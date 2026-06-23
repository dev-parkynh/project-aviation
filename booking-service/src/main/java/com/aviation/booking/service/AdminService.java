package com.aviation.booking.service;

import com.aviation.booking.dto.PassengerDto;
import com.aviation.booking.dto.ReservationDto;
import com.aviation.booking.dto.StatsDto;
import com.aviation.booking.entity.Reservation;
import com.aviation.booking.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final ReservationRepository reservationRepository;

    public List<ReservationDto.Response> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationDto.Response updateReservationStatus(Long id, Reservation.ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
        reservation.setStatus(status);
        return toResponse(reservation);
    }

    public List<StatsDto.DailyStats> getDailyStats() {
        return reservationRepository.findDailyStats(Reservation.ReservationStatus.CONFIRMED).stream()
                .map(row -> StatsDto.DailyStats.builder()
                        .date(LocalDate.of(
                                ((Number) row[0]).intValue(),
                                ((Number) row[1]).intValue(),
                                ((Number) row[2]).intValue()))
                        .reservationCount(((Number) row[3]).longValue())
                        .totalRevenue(toBigDecimal(row[4]))
                        .build())
                .collect(Collectors.toList());
    }

    public List<StatsDto.MonthlyStats> getMonthlyStats() {
        return reservationRepository.findMonthlyStats(Reservation.ReservationStatus.CONFIRMED).stream()
                .map(row -> StatsDto.MonthlyStats.builder()
                        .year(((Number) row[0]).intValue())
                        .month(((Number) row[1]).intValue())
                        .reservationCount(((Number) row[2]).longValue())
                        .totalRevenue(toBigDecimal(row[3]))
                        .build())
                .collect(Collectors.toList());
    }

    public List<StatsDto.RouteStats> getRouteStats() {
        return reservationRepository.findRouteStats(Reservation.ReservationStatus.CONFIRMED).stream()
                .map(row -> StatsDto.RouteStats.builder()
                        .origin((String) row[0])
                        .destination((String) row[1])
                        .reservationCount(((Number) row[2]).longValue())
                        .totalRevenue(toBigDecimal(row[3]))
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
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
