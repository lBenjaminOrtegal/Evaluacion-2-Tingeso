package com.tingeso.m4service.controllers;

import com.tingeso.m4service.entities.Reservation;
import com.tingeso.m4service.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
public class ReservationInternalController {

    private final ReservationService reservationService;

    @GetMapping("/by-package/{tourPackageId}")
    public ResponseEntity<List<Reservation>> findByTourPackageId(@PathVariable Long tourPackageId) {
        return ResponseEntity.ok(reservationService.findByTourPackageId(tourPackageId));
    }
}
