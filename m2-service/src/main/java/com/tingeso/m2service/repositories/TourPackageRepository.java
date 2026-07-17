package com.tingeso.m2service.repositories;

import com.tingeso.m2service.entities.TourPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackage, Long> {}