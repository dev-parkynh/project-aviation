package com.aviation.booking.scheduler;

import com.aviation.booking.entity.Reservation;
import com.aviation.booking.feign.FlightServiceClient;
import com.aviation.booking.feign.SeatReserveRequest;
import com.aviation.booking.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private static final long PENDING_EXPIRY_MINUTES = 30;

    private final ReservationRepository reservationRepository;
    private final FlightServiceClient flightServiceClient;

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void expireUnpaidReservations() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(PENDING_EXPIRY_MINUTES);
        List<Reservation> targets = reservationRepository.findByStatusAndCreatedAtBefore(
                Reservation.ReservationStatus.PENDING, expiredBefore);

        if (targets.isEmpty()) {
            return;
        }

        for (Reservation reservation : targets) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            try {
                flightServiceClient.releaseSeat(
                        reservation.getSeatId(),
                        new SeatReserveRequest(reservation.getFlightId())
                );
            } catch (Exception e) {
                log.warn("[Scheduler] 좌석 반환 실패 - reservationId={}, seatId={}: {}",
                        reservation.getId(), reservation.getSeatId(), e.getMessage());
            }
        }
        log.info("[Scheduler] Expired {} unpaid reservation(s) (threshold: {}min)",
                targets.size(), PENDING_EXPIRY_MINUTES);
    }
}
