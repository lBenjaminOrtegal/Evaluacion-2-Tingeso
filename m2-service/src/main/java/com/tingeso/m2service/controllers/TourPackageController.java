package com.tingeso.m2service.controllers;

import com.tingeso.m2service.entities.TourPackage;
import com.tingeso.m2service.services.TourPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour-packages")
@RequiredArgsConstructor
public class TourPackageController {

    private final TourPackageService tourPackageService;

    @GetMapping
    public ResponseEntity<List<TourPackage>> findAll() {
        return ResponseEntity.ok(tourPackageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourPackage> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tourPackageService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TourPackage> create(@Valid @RequestBody TourPackage tourPackage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourPackageService.createTourPackage(tourPackage));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<TourPackage> update(@Valid @RequestBody TourPackage tourPackage) {
        return ResponseEntity.ok(tourPackageService.update(tourPackage));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable Long id) {
        tourPackageService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
