package com.aviation.booking.repository;

import com.aviation.booking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserEmail(String userEmail);

    List<Reservation> findByStatusAndCreatedAtBefore(
            Reservation.ReservationStatus status,
            LocalDateTime expiredBefore);

    @Query("SELECT r.origin, r.destination, COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.status = :status " +
           "AND YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month " +
           "GROUP BY r.origin, r.destination " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findMonthlyRouteStats(
            @Param("status") Reservation.ReservationStatus status,
            @Param("year") int year,
            @Param("month") int month);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), DAY(r.createdAt), COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.status = :status " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt), DAY(r.createdAt) " +
           "ORDER BY YEAR(r.createdAt), MONTH(r.createdAt), DAY(r.createdAt)")
    List<Object[]> findDailyStats(@Param("status") Reservation.ReservationStatus status);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.status = :status " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) " +
           "ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> findMonthlyStats(@Param("status") Reservation.ReservationStatus status);

    @Query("SELECT r.origin, r.destination, COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.status = :status " +
           "GROUP BY r.origin, r.destination " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findRouteStats(@Param("status") Reservation.ReservationStatus status);
}
