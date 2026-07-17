package com.tingeso.m3service.controllers;

import com.tingeso.m3service.dto.TourPackageFiltersDTO;
import com.tingeso.m3service.entities.TourPackage;
import com.tingeso.m3service.enums.Category;
import com.tingeso.m3service.enums.Season;
import com.tingeso.m3service.enums.TourPackageState;
import com.tingeso.m3service.enums.TripType;
import com.tingeso.m3service.services.TourPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search/tour-packages")
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

    @GetMapping("/filters")
    public ResponseEntity<List<TourPackage>> findCustomFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String destiny,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Season season,
            @RequestParam(required = false) TripType tripType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TourPackageState state) {
        TourPackageFiltersDTO tourPackageFiltersDTO = new TourPackageFiltersDTO(
                name,
                destiny,
                category,
                season,
                tripType,
                maxPrice,
                startDate,
                endDate,
                state
        );
        return ResponseEntity.ok(tourPackageService.findCustomFilters(tourPackageFiltersDTO));
    }

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
