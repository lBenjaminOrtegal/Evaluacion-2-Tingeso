package com.tingeso.m2service.services;

import com.tingeso.m2service.entities.TourPackage;
import com.tingeso.m2service.enums.TourPackageState;
import com.tingeso.m2service.exceptions.BusinessRuleException;
import com.tingeso.m2service.exceptions.ResourceNotFoundException;
import com.tingeso.m2service.models.Reservation;
import com.tingeso.m2service.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourPackageService {

    private final TourPackageRepository tourPackageRepository;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public List<Reservation> m4_findByTourPackageId(Long id) { // reservations
        try {
            Reservation[] arr = restTemplate.getForObject("http://m4-service/internal/reservations/by-package/" + id, Reservation[].class);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Cannot verify existing reservations.");
        }
    }

    private void syncPostToM3(TourPackage tourPackage) {
        try {
            restTemplate.postForObject(
                    "http://m3-service/internal/search/tour-packages/sync",
                    tourPackage,
                    Void.class
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while synchronizing tour package post.");
        }
    }

    private void syncDeleteToM3(Long id) {
        try {
            restTemplate.delete("http://m3-service/internal/search/tour-packages/sync/" + id);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while synchronizing tour package delete.");
        }
    }

    @Transactional(readOnly = true)
    public List<TourPackage> findAll() {
        return tourPackageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TourPackage findById(Long id) {
        return tourPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour package with id " + id + " not found"));
    }

    @Transactional
    public TourPackage createTourPackage(TourPackage tourPackage) {
        if (tourPackage.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Price must be greater than zero");
        }
        if (tourPackage.getEndDate().isBefore(tourPackage.getStartDate())) {
            throw new BusinessRuleException("Start date must be before end date");
        }
        if (tourPackage.getInitialSpots() <= 0) {
            throw new BusinessRuleException("Initial spots must be greater than zero");
        }
        tourPackage.calculateDuration();
        TourPackage saved = tourPackageRepository.save(tourPackage);
        syncPostToM3(saved);
        return saved;
    }

    @Transactional
    public TourPackage justSave(TourPackage tourPackage) {
        TourPackage saved = tourPackageRepository.save(tourPackage);
        syncPostToM3(saved);
        return saved;
    }

    @Transactional
    public TourPackage update(TourPackage tourPackage) {
        TourPackage existingPackage = tourPackageRepository.findById(tourPackage.getId())
                .orElseThrow(() -> new ResourceNotFoundException("TourPackage not found with id: " + tourPackage.getId()));
        List<Reservation> reservations = m4_findByTourPackageId(tourPackage.getId());
        existingPackage.setName(tourPackage.getName());
        existingPackage.setPrice(tourPackage.getPrice());
        existingPackage.setSeason(tourPackage.getSeason());
        existingPackage.setCategory(tourPackage.getCategory());
        existingPackage.setTripType(tourPackage.getTripType());
        existingPackage.setServices(tourPackage.getServices());
        existingPackage.setConditions(tourPackage.getConditions());
        existingPackage.setRestrictions(tourPackage.getRestrictions());
        existingPackage.setDescription(tourPackage.getDescription());
        int occupiedSpots = existingPackage.getInitialSpots() - existingPackage.getRemainingSpots();
        if (!reservations.isEmpty()) { // if had reservations
            if (tourPackage.getInitialSpots() < occupiedSpots) {
                throw new BusinessRuleException("New initial spots are less tan occupied spots.");
            } else {
                existingPackage.setInitialSpots(tourPackage.getInitialSpots());
                existingPackage.setRemainingSpots(tourPackage.getInitialSpots() - occupiedSpots);
            }
        } else {
            existingPackage.setDestiny(tourPackage.getDestiny());
            existingPackage.setStartDate(tourPackage.getStartDate());
            existingPackage.setEndDate(tourPackage.getEndDate());
            existingPackage.calculateDuration();
            existingPackage.setInitialSpots(tourPackage.getInitialSpots());
            existingPackage.setRemainingSpots(tourPackage.getInitialSpots());
        }
        if (existingPackage.getRemainingSpots() <= 0) {
            existingPackage.setTourPackageState(TourPackageState.SOLD_OUT);
        } else {
            existingPackage.setTourPackageState(tourPackage.getTourPackageState());
        }
        TourPackage saved = tourPackageRepository.save(existingPackage);
        syncPostToM3(saved);
        return saved;
    }

    @Transactional
    public void deleteById(Long id) throws EmptyResultDataAccessException {
        TourPackage tourPackage = tourPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TourPackage not found with id: " + id));
        List<Reservation> reservations = m4_findByTourPackageId(id);
        if (reservations.isEmpty()) {
            syncDeleteToM3(id);
            tourPackageRepository.deleteById(id);
        } else {
            tourPackage.setTourPackageState(TourPackageState.NOT_AVAILABLE);
            TourPackage saved = tourPackageRepository.save(tourPackage);
            syncPostToM3(saved);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateStartedTourPackages() {
        List<TourPackage> tourPackageList = tourPackageRepository.findAll();
        List<TourPackage> updated = new ArrayList<>();
        for (TourPackage tourPackage : tourPackageList) {
            if (tourPackage.getStartDate().isAfter(LocalDate.now()) || tourPackage.getStartDate().isEqual(LocalDate.now())) {
                tourPackage.setTourPackageState(TourPackageState.NOT_AVAILABLE);
                updated.add(tourPackage);
            }
        }
        if (!updated.isEmpty()) {
            tourPackageRepository.saveAll(updated);
            syncUpdatedTourPackages(updated);
        }
    }

    private void syncUpdatedTourPackages(List<TourPackage> updated) {
        for (TourPackage tourPackage : updated) {
            syncPostToM3(tourPackage);
        }
    }
}
