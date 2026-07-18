package com.tingeso.m4service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourPackage {
    private Long id;
    private String name;
    private String destiny;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String duration;
    private BigDecimal price;
    private List<String> services;
    private List<String> conditions;
    private List<String> restrictions;
    private Integer initialSpots;
    private Integer remainingSpots;
    private String tripType;
    private String season;
    private String category;
    private String tourPackageState;
}
