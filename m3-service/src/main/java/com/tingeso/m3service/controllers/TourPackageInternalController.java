package com.tingeso.m3service.controllers;

import com.tingeso.m3service.entities.TourPackage;
import com.tingeso.m3service.services.TourPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/search/tour-packages")
@RequiredArgsConstructor
public class TourPackageInternalController {

    private final TourPackageService tourPackageService;

    @PostMapping("/sync")
    public ResponseEntity<Void> sync(@RequestBody TourPackage tourPackage) {
        tourPackageService.syncCopy(tourPackage);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sync/{id}")
    public ResponseEntity<Void> syncDelete(@PathVariable Long id) {
        tourPackageService.syncDeleteCopy(id);
        return ResponseEntity.ok().build();
    }
}
