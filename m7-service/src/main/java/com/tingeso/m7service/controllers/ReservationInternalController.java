package com.tingeso.m7service.controllers;

import com.tingeso.m7service.entities.Reservation;
import com.tingeso.m7service.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reports")
@RequiredArgsConstructor
public class ReservationInternalController {

    private final ReservationService reservationService;

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
