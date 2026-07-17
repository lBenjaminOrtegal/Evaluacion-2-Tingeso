package com.tingeso.m2service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long id;
    private String userEmail;
    private Long tourPackageId;
    private String tourPackageName;
    private String reservationState;
    private BigDecimal price;
    private LocalDateTime reservationDate;
    private LocalDateTime paymentDate;
    private Integer passengersAmount;
    private List<String> preferences;
    private List<String> specialRequests;
}
