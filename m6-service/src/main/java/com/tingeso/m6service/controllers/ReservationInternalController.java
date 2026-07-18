package com.tingeso.m6service.controllers;

import com.tingeso.m6service.entities.Reservation;
import com.tingeso.m6service.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/follow/reservations")
@RequiredArgsConstructor
public class ReservationInternalController {

    private final ReservationService reservationService;

    @GetMapping("/by-package/{tourPackageId}")
    public ResponseEntity<List<Reservation>> findByTourPackageId(@PathVariable Long tourPackageId) {
        return ResponseEntity.ok(reservationService.findByTourPackageId(tourPackageId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> sync(@RequestBody Reservation reservation) {
        reservationService.syncCopy(reservation);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sync/{id}")
    public ResponseEntity<Void> syncDelete(@PathVariable Long id) {
        reservationService.syncDeleteCopy(id);
        return ResponseEntity.ok().build();
    }
}
