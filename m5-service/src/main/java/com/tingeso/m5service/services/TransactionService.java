package com.tingeso.m5service.services;

import com.tingeso.m5service.entities.Transaction;
import com.tingeso.m5service.enums.ReservationState;
import com.tingeso.m5service.enums.TransactionState;
import com.tingeso.m5service.exceptions.BusinessRuleException;
import com.tingeso.m5service.exceptions.ResourceNotFoundException;
import com.tingeso.m5service.models.Reservation;
import com.tingeso.m5service.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    public Reservation m4_findById(Long id) {
        try {
            return restTemplate.getForObject(
                    "http://m4-service/internal/reservations/" + id,
                    Reservation.class);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while looking for reservation.");
        }
    }

    public void m4_justSave(Reservation reservation) {
        try {
            restTemplate.put(
                    "http://m4-service/internal/reservations/just-save",
                    reservation
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while updating reservation.");
        }
    }

    @Transactional(readOnly = true)
    public Transaction findByReservationId(Long id) {
        return transactionRepository.findByReservationId(id);
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        Reservation reservation = m4_findById(transaction.getReservationId());
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation not found with id " + transaction.getReservationId());
        }
        if (Objects.equals(reservation.getReservationState(), ReservationState.CANCELED.toString())) {
            throw new BusinessRuleException("Cannot create transaction because reservation is canceled.");
        }
        if (reservation.getPaymentDate() != null) {
            throw new BusinessRuleException("Cannot create transaction because payment is already set.");
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Cannot create transaction because amount is less or equal to 0.");
        }
        reservation.setReservationState(String.valueOf(ReservationState.CONFIRMED));
        reservation.setPaymentDate(LocalDateTime.now());
        transaction.setDate(LocalDateTime.now());
        transaction.setState(TransactionState.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);
        m4_justSave(reservation);
        return saved;
    }

    public Boolean successfulTransaction() {
        return true;
    }
}
