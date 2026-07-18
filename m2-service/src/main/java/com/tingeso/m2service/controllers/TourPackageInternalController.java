package com.tingeso.m2service.controllers;

import com.tingeso.m2service.entities.TourPackage;
import com.tingeso.m2service.services.TourPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/tour-packages")
@RequiredArgsConstructor
public class TourPackageInternalController {

    private final TourPackageService tourPackageService;

    @PutMapping("/just-save")
    public ResponseEntity<TourPackage> justSave(@RequestBody TourPackage tourPackage) {
        return ResponseEntity.ok(tourPackageService.justSave(tourPackage));
    }
}
