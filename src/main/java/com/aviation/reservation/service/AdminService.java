package com.aviation.reservation.service;

import com.aviation.reservation.dto.PassengerDto;
import com.aviation.reservation.dto.ReservationDto;
import com.aviation.reservation.dto.StatsDto;
import com.aviation.reservation.entity.Reservation;
import com.aviation.reservation.repository.ReservationRepository;
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

        Reservation.ReservationStatus current = reservation.getStatus();

        // CONFIRMED → CANCELLED: 좌석 복구
        if (status == Reservation.ReservationStatus.CANCELLED && current == Reservation.ReservationStatus.CONFIRMED) {
            reservation.getSeat().setIsAvailable(true);
            reservation.getFlight().setAvailableSeats(reservation.getFlight().getAvailableSeats() + 1);
        }
        // CANCELLED → CONFIRMED: 좌석 재점유
        else if (status == Reservation.ReservationStatus.CONFIRMED && current == Reservation.ReservationStatus.CANCELLED) {
            if (!reservation.getSeat().getIsAvailable()) {
                throw new IllegalStateException("Seat is no longer available: " + reservation.getSeat().getSeatNo());
            }
            reservation.getSeat().setIsAvailable(false);
            reservation.getFlight().setAvailableSeats(reservation.getFlight().getAvailableSeats() - 1);
        }

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
