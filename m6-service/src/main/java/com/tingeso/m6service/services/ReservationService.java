package com.tingeso.m6service.services;

import com.tingeso.m6service.entities.Reservation;
import com.tingeso.m6service.exceptions.BusinessRuleException;
import com.tingeso.m6service.exceptions.ResourceNotFoundException;
import com.tingeso.m6service.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByUserEmail(String userEmail) {
        return reservationRepository.findByUserEmail(userEmail);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByTourPackageId(Long tourPackageId) {
        return reservationRepository.findByTourPackageId(tourPackageId);
    }

    @Transactional
    public Reservation update(Reservation reservation) {
        try {
            ResponseEntity<Reservation> response = restTemplate.exchange(
                    "http://m4-service/internal/reservations",
                    HttpMethod.PUT,
                    new HttpEntity<>(reservation),
                    Reservation.class
            );
            Reservation updatedReservation= response.getBody();
            if (updatedReservation == null) {
                throw new BusinessRuleException("No response from reservations service.");
            }
            return reservationRepository.save(updatedReservation);
        } catch (HttpClientErrorException e) {
            throw new BusinessRuleException("Cannot update reservation: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while updating reservation.");
        }
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            restTemplate.exchange(
                    "http://m4-service/internal/reservations/" + id,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );
            reservationRepository.deleteById(id);
        } catch (HttpClientErrorException e) {
            throw new BusinessRuleException("Cannot delete reservation: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while deleting reservation.");
        }
    }

    @Transactional
    public void syncCopy(Reservation reservation) {
        reservationRepository.save(reservation);
    }

    @Transactional
    public void syncDeleteCopy(Long id) {
        reservationRepository.deleteById(id);
    }
}
