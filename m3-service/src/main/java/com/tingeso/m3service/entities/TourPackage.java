package com.tingeso.m3service.entities;

import com.tingeso.m3service.enums.Category;
import com.tingeso.m3service.enums.Season;
import com.tingeso.m3service.enums.TourPackageState;
import com.tingeso.m3service.enums.TripType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourPackage {

    @Id
    private Long id;

    @NotBlank(message = "Name cannot be null")
    private String name;

    @NotBlank(message = "Destiny cannot be null")
    private String destiny;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;

    @NotNull(message = "Start date cannot be null")
    private LocalDate endDate;

    private String duration;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @ElementCollection
    private List<String> services = new ArrayList<>();

    @ElementCollection
    private List<String> conditions = new ArrayList<>();

    @ElementCollection
    private List<String> restrictions = new ArrayList<>();

    @NotNull(message = "Initial spots cannot be null")
    @Positive(message = "Initial spots must be greater than 0")
    private Integer initialSpots;

    private Integer remainingSpots;

    @Enumerated(EnumType.STRING)
    private TripType tripType;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private TourPackageState tourPackageState;
}
