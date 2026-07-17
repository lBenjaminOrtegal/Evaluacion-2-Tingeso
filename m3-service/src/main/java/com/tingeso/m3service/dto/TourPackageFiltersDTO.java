package com.tingeso.m3service.dto;

import com.tingeso.m3service.enums.Category;
import com.tingeso.m3service.enums.Season;
import com.tingeso.m3service.enums.TourPackageState;
import com.tingeso.m3service.enums.TripType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TourPackageFiltersDTO(
        String name,
        String destiny,
        Category category,
        Season season,
        TripType tripType,
        BigDecimal maxPrice,
        LocalDate startDate,
        LocalDate endDate,
        TourPackageState state
) {}
