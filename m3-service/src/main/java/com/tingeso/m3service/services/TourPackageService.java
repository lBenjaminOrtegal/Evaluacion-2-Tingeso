package com.tingeso.m3service.services;

import com.tingeso.m3service.dto.TourPackageFiltersDTO;
import com.tingeso.m3service.entities.TourPackage;
import com.tingeso.m3service.exceptions.ResourceNotFoundException;
import com.tingeso.m3service.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourPackageService {

    private final TourPackageRepository tourPackageRepository;

    @Transactional(readOnly = true)
    public List<TourPackage> findAll() {
        return tourPackageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TourPackage findById(Long id) {
        return tourPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour package with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<TourPackage> findCustomFilters(TourPackageFiltersDTO tourPackageFiltersDTO) {
        return tourPackageRepository.findCustomFilters(tourPackageFiltersDTO);
    }

    @Transactional
    public void syncCopy(TourPackage tourPackage) {
        tourPackageRepository.save(tourPackage);
    }

    @Transactional
    public void syncDeleteCopy(Long id) {
        tourPackageRepository.deleteById(id);
    }
}
