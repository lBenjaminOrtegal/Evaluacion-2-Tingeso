package com.tingeso.m4service.repositories;

import com.tingeso.m4service.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByTourPackageId(Long tourPackageId);
}
