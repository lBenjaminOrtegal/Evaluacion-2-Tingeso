package com.tingeso.m4service;

import com.tingeso.m4service.entities.Discount;
import com.tingeso.m4service.repositories.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class M4ServiceApplication implements CommandLineRunner {

    private final DiscountRepository discountRepository;

    public static void main(String[] args) {
        SpringApplication.run(M4ServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (!discountRepository.existsById(1L)) {
            Discount defaultDiscounts = new Discount(
                    1L,
                    true,
                    new BigDecimal("0.25"),
                    4,
                    new BigDecimal("0.05"),
                    3,
                    new BigDecimal("0.10"),
                    7,
                    3,
                    new BigDecimal("0.15")
            );

            discountRepository.save(defaultDiscounts);
            System.out.println("Discount created");
        } else {
            System.out.println("Discount already exists");
        }
    }
}
