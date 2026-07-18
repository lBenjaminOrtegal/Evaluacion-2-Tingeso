package com.tingeso.m5service.repositories;

import com.tingeso.m5service.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByReservationId(Long id);
}
