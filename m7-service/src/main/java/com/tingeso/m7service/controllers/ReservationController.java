package com.tingeso.m7service.controllers;

import com.tingeso.m7service.entities.Reservation;
import com.tingeso.m7service.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/date")
    public ResponseEntity<List<Reservation>> findDateReports(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(reservationService.findDateReports(startDate, endDate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ranking")
    public ResponseEntity<List<List<Reservation>>> findRanking(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate, @RequestParam Integer order, @RequestParam String type) {
        return ResponseEntity.ok(reservationService.findRanking(startDate, endDate, order, type));
    }
}
