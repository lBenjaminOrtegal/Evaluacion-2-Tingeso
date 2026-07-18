package com.tingeso.m4service.services;

import com.tingeso.m4service.configuration.DiscountConfig;
import com.tingeso.m4service.dto.DiscountDataDTO;
import com.tingeso.m4service.entities.Promotion;
import com.tingeso.m4service.entities.Reservation;
import com.tingeso.m4service.enums.ReservationState;
import com.tingeso.m4service.enums.TourPackageState;
import com.tingeso.m4service.exceptions.BusinessRuleException;
import com.tingeso.m4service.exceptions.ResourceNotFoundException;
import com.tingeso.m4service.models.TourPackage;
import com.tingeso.m4service.repositories.PromotionRepository;
import com.tingeso.m4service.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate;
    private final PromotionRepository promotionRepository;
    private final DiscountService discountService;
    private final DiscountConfig discountConfig;

    public TourPackage m2_FindById(Long id) {
        try {
            return restTemplate.getForObject(
                    "http://m2-service/api/tour-packages/" + id,
                    TourPackage.class
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while looking for tour package.");
        }
    }

    public void m2_justSave(TourPackage tourPackage) {
        try {
            restTemplate.put(
                    "http://m2-service/internal/tour-packages/just-save",
                    tourPackage
            );
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error while updating tour package.");
        }
    }

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
    public Reservation create(Reservation reservation) {
        TourPackage tourPackage = m2_FindById(reservation.getTourPackageId());
        if (tourPackage == null) {
            throw new ResourceNotFoundException("Tour package not found with id " + reservation.getTourPackageId());
        }
        if (!Objects.equals(tourPackage.getTourPackageState(), TourPackageState.AVAILABLE.toString())) {
            throw new BusinessRuleException("Tour package not available for reservations");
        }
        tourPackage.setRemainingSpots(tourPackage.getRemainingSpots() - reservation.getPassengersAmount());
        if (tourPackage.getRemainingSpots() <= 0) {
            tourPackage.setTourPackageState(String.valueOf(TourPackageState.SOLD_OUT));
        }
        reservation.setPrice(calculatePrice(reservation).getTotalAmount());
        reservation.setTourPackageName(tourPackage.getName());
        reservation.setReservationDate(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        m2_justSave(tourPackage);
        return saved;
    }

    @Transactional
    public Reservation justSave(Reservation reservation) {
        Reservation saved = reservationRepository.save(reservation);
//        syncPostToM6(saved);
        return saved;
    }

    @Transactional
    public Reservation update(Reservation reservation) {
        Reservation reservationSaved = reservationRepository.findById(reservation.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + reservation.getId()));
        TourPackage tourPackage = m2_FindById(reservationSaved.getTourPackageId());
        if (tourPackage == null) {
            throw new ResourceNotFoundException("Tour package not found with id " + reservation.getTourPackageId());
        }
        if (reservationSaved.getReservationState() == ReservationState.CANCELED) {
            throw new BusinessRuleException("Cannot modify reservation because reservation is already canceled.");
        }
        if (reservation.getReservationState() == ReservationState.CONFIRMED
                || reservation.getReservationState() == ReservationState.COMPLETED
                || reservation.getReservationState() == ReservationState.IN_PROGRESS) {
            if (reservationSaved.getPaymentDate() != null) {
                reservationSaved.setReservationState(reservation.getReservationState());
            } else {
                throw new BusinessRuleException("Cannot modify reservation because payment date is null (not transaction done).");
            }
        }
        if (reservation.getReservationState() == ReservationState.CANCELED) {
            tourPackage.setRemainingSpots(tourPackage.getRemainingSpots() + reservationSaved.getPassengersAmount());
            tourPackage.setTourPackageState(String.valueOf(TourPackageState.AVAILABLE));
        } else if (!Objects.equals(reservationSaved.getPassengersAmount(), reservation.getPassengersAmount())) {
            int difference = reservation.getPassengersAmount() - reservationSaved.getPassengersAmount();
            if (tourPackage.getRemainingSpots() < difference) {
                throw new BusinessRuleException("Not enough spots for reservation.");
            }
            tourPackage.setRemainingSpots(tourPackage.getRemainingSpots() - difference);
            tourPackage.setTourPackageState(String.valueOf(tourPackage.getRemainingSpots() <= 0
                    ? TourPackageState.SOLD_OUT
                    : TourPackageState.AVAILABLE));
        }
        reservationSaved.setPreferences(reservation.getPreferences());
        reservationSaved.setSpecialRequests(reservation.getSpecialRequests());
        reservationSaved.setPassengersAmount(reservation.getPassengersAmount());
        reservationSaved.setReservationState(reservation.getReservationState());
        reservationSaved.setPrice(calculatePrice(reservation).getTotalAmount());
        Reservation saved = reservationRepository.save(reservationSaved);
        m2_justSave(tourPackage);
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservationSaved = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        TourPackage tourPackage = m2_FindById(reservationSaved.getTourPackageId());
        if (tourPackage == null) {
            throw new ResourceNotFoundException("Tour package not found with id " + reservationSaved.getTourPackageId());
        }
        if (reservationSaved.getReservationState() == ReservationState.CANCELED) {
            reservationRepository.deleteById(id);
            return;
        }
        tourPackage.setRemainingSpots(tourPackage.getRemainingSpots() + reservationSaved.getPassengersAmount());
        tourPackage.setTourPackageState(String.valueOf(TourPackageState.AVAILABLE));
        reservationRepository.deleteById(id);
        m2_justSave(tourPackage);
    }

    public DiscountDataDTO calculatePrice(Reservation reservation) {
        TourPackage tourPackage = m2_FindById(reservation.getTourPackageId());
        if (tourPackage == null) {
            throw new ResourceNotFoundException("Tour package not found with id " + reservation.getTourPackageId());
        }
        DiscountDataDTO discountDataDTO = new DiscountDataDTO();
        BigDecimal basePrice = tourPackage.getPrice();
        BigDecimal totalWithoutDiscounts = basePrice.multiply(BigDecimal.valueOf(reservation.getPassengersAmount()));

        discountDataDTO.setTotalAmountWithoutDiscounts(totalWithoutDiscounts.setScale(2, RoundingMode.HALF_UP));

        BigDecimal passengersDiscountPercentage = discountService.calculatePassengersAmountDiscount(reservation.getPassengersAmount());
        BigDecimal frequentClientDiscountPercentage = discountService.calculateFrequentClientDiscount(reservation.getUserEmail());
        BigDecimal multiplePackagesDiscountPercentage = discountService.calculateMultiplePackagesDiscount(reservation.getUserEmail());

        BigDecimal promotionDiscountPercentage = promotionRepository.findByTourPackageId(reservation.getTourPackageId())
                .map(Promotion::getDiscount)
                .orElse(BigDecimal.ZERO);

        discountDataDTO.setPassengersDiscount(totalWithoutDiscounts.multiply(passengersDiscountPercentage).setScale(2, RoundingMode.HALF_UP));
        discountDataDTO.setFrequentClientDiscount(totalWithoutDiscounts.multiply(frequentClientDiscountPercentage).setScale(2, RoundingMode.HALF_UP));
        discountDataDTO.setMultiplePackagesDiscount(totalWithoutDiscounts.multiply(multiplePackagesDiscountPercentage).setScale(2, RoundingMode.HALF_UP));
        discountDataDTO.setPromotionDiscount(totalWithoutDiscounts.multiply(promotionDiscountPercentage).setScale(2, RoundingMode.HALF_UP));

        BigDecimal accumulatedPercentage;
        if (discountConfig.isCombinableDiscounts()) {
            accumulatedPercentage = passengersDiscountPercentage.add(frequentClientDiscountPercentage)
                    .add(multiplePackagesDiscountPercentage)
                    .add(promotionDiscountPercentage);
        } else {
            accumulatedPercentage = passengersDiscountPercentage.max(frequentClientDiscountPercentage)
                    .max(multiplePackagesDiscountPercentage)
                    .max(promotionDiscountPercentage);
        }

        if (accumulatedPercentage.subtract(discountConfig.getMaxDiscountLimit()).compareTo(BigDecimal.ZERO) > 0) {
            accumulatedPercentage = discountConfig.getMaxDiscountLimit();
            discountDataDTO.setMaxDiscount(true);
        } else {
            discountDataDTO.setMaxDiscount(false);
        }

        BigDecimal finalDiscountAmount = totalWithoutDiscounts.multiply(accumulatedPercentage);
        BigDecimal totalWithDiscounts = totalWithoutDiscounts.subtract(finalDiscountAmount);

        discountDataDTO.setDiscountAmount(finalDiscountAmount.setScale(2, RoundingMode.HALF_UP));
        discountDataDTO.setTotalAmount(totalWithDiscounts.setScale(2, RoundingMode.HALF_UP));

        return discountDataDTO;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cancelExpiredReservations() {
        LocalDateTime limit = LocalDateTime.now().minusHours(24);
        List<Reservation> expiredReservations = reservationRepository
                .findByReservationStateAndReservationDateBefore(ReservationState.PENDING, limit);
        if (expiredReservations.isEmpty()) {
            return;
        }
        expiredReservations.forEach(reservation -> {
            reservation.setReservationState(ReservationState.CANCELED);
            TourPackage tourPackage = m2_FindById(reservation.getTourPackageId());
            if (tourPackage != null) {
                tourPackage.setRemainingSpots(tourPackage.getRemainingSpots() + reservation.getPassengersAmount());
                tourPackage.setTourPackageState(String.valueOf(TourPackageState.AVAILABLE));
                m2_justSave(tourPackage);
            }
        });
        reservationRepository.saveAll(expiredReservations);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void setReservationsState() {
        LocalDate today = LocalDate.now();
        List<Reservation> reservations = reservationRepository.findAll();
        reservations.forEach(reservation -> {
            TourPackage tourPackage = m2_FindById(reservation.getTourPackageId());
            if (tourPackage != null) {
                if (reservation.getReservationState() == ReservationState.CONFIRMED || reservation.getReservationState() == ReservationState.IN_PROGRESS) {
                    if ((today.isEqual(tourPackage.getStartDate()) || today.isAfter(tourPackage.getStartDate()))
                            && today.isBefore(tourPackage.getEndDate())) {
                        reservation.setReservationState(ReservationState.IN_PROGRESS);
                    } else if (today.isEqual(tourPackage.getEndDate()) || today.isAfter(tourPackage.getEndDate())) {
                        reservation.setReservationState(ReservationState.COMPLETED);
                    }
                }
            }
        });
        reservationRepository.saveAll(reservations);
    }
}
