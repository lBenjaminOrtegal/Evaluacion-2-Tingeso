package com.tingeso.m6service.controllers;

import com.tingeso.m6service.entities.Reservation;
import com.tingeso.m6service.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Reservation>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user-email/{userEmail}")
    public ResponseEntity<List<Reservation>> findByUserEmail(@PathVariable String userEmail) {
        return ResponseEntity.ok(reservationService.findByUserEmail(userEmail));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping
    public ResponseEntity<Reservation> update(@Valid @RequestBody Reservation reservation) {
        return ResponseEntity.ok(reservationService.update(reservation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
