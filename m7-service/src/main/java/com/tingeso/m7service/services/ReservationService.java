package com.tingeso.m7service.services;

import com.tingeso.m7service.entities.Reservation;
import com.tingeso.m7service.enums.ReservationState;
import com.tingeso.m7service.exceptions.BusinessRuleException;
import com.tingeso.m7service.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<Reservation> findDateReports(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessRuleException("Start date must be before end date");
        }
        LocalDateTime startDateTime = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endDateTime = endDate.withHour(23).withMinute(59).withSecond(59).withNano(0);
        return reservationRepository.findDateReports(startDateTime, endDateTime, ReservationState.CANCELED);
    }

    @Transactional(readOnly = true)
    public List<List<Reservation>> findRanking(LocalDateTime startDate, LocalDateTime endDate, Integer order, String type) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessRuleException("Start date must be before end date");
        }
        List<Reservation> reservations = findDateReports(startDate, endDate);
        Map<Long, List<Reservation>> groupedByPackage = reservations
                .stream()
                .collect(Collectors.groupingBy(Reservation::getTourPackageId));
        List<List<Reservation>> ranking = new ArrayList<>(groupedByPackage.values());
        ranking.sort((groupA, groupB) -> {
            int result;
            if ("passengers".equalsIgnoreCase(type)) {
                Long totalA = groupA.stream().mapToLong(Reservation::getPassengersAmount).sum();
                Long totalB = groupB.stream().mapToLong(Reservation::getPassengersAmount).sum();
                result = totalA.compareTo(totalB);
            } else {
                result = Integer.compare(groupA.size(), groupB.size());
            }
            if (result == 0) {
                BigDecimal revenueA = groupA.stream()
                        .map(Reservation::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal revenueB = groupB.stream()
                        .map(Reservation::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                result = revenueA.compareTo(revenueB);
            }
            if (result == 0 && !groupA.isEmpty() && !groupB.isEmpty()) {
                String nameA = groupA.getFirst().getTourPackageName();
                String nameB = groupB.getFirst().getTourPackageName();
                result = nameA.compareToIgnoreCase(nameB);
            }
            return (order == 1) ? result * -1 : result;
        });
        return ranking;
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
