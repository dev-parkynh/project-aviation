package com.aviation.reservation.scheduler;

import com.aviation.reservation.entity.Reservation;
import com.aviation.reservation.repository.ReservationRepository;
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

    // 미결제 예약 자동 만료 — 10분마다 실행
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
            reservation.getSeat().setIsAvailable(true);
            reservation.getFlight().setAvailableSeats(
                    reservation.getFlight().getAvailableSeats() + 1);
        }
        log.info("[Scheduler] Expired {} unpaid reservation(s) (threshold: {}min)",
                targets.size(), PENDING_EXPIRY_MINUTES);
    }
}
