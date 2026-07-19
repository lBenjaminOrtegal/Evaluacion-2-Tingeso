package com.tingeso.m7service.repositories;

import com.tingeso.m7service.entities.Reservation;
import com.tingeso.m7service.enums.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.reservationState <> :excludedState AND (" +
            "r.paymentDate BETWEEN :start AND :end OR " +
            "r.reservationDate BETWEEN :start AND :end)")
    List<Reservation> findDateReports(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludedState") ReservationState excludedState
    );
}
