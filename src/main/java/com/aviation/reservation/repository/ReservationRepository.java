package com.aviation.reservation.repository;

import com.aviation.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByUserEmail(String email);

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

    @Query("SELECT r.flight.origin, r.flight.destination, COUNT(r), COALESCE(SUM(r.totalPrice), 0) " +
           "FROM Reservation r WHERE r.status = :status " +
           "GROUP BY r.flight.origin, r.flight.destination " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> findRouteStats(@Param("status") Reservation.ReservationStatus status);
}
