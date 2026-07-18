package com.tingeso.m4service.repositories;

import com.tingeso.m4service.entities.Reservation;
import com.tingeso.m4service.enums.ReservationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserEmail(String userEmail);

    List<Reservation> findByTourPackageId(Long id);

    List<Reservation> findByReservationStateAndReservationDateBefore(ReservationState reservationState, LocalDateTime limit);
}
