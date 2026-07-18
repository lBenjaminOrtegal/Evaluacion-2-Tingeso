package com.tingeso.m4service.controllers;

import com.tingeso.m4service.entities.Reservation;
import com.tingeso.m4service.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @PutMapping("/just-save")
    public ResponseEntity<Reservation> justSave(@RequestBody Reservation reservation) {
        return ResponseEntity.ok(reservationService.justSave(reservation));
    }
}
