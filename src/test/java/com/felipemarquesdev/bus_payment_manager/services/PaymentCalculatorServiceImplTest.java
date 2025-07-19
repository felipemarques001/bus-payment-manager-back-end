package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.exceptions.DiscountExceedsTotalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentCalculatorServiceImplTest {

    @InjectMocks
    private PaymentCalculatorServiceImpl paymentCalculatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Given total amount and financial help amounts. when calculateAmountToBePaid(), then return amount to be paid")
    void calculateAmountToBePaidSuccessCase() {
        // Given
        BigDecimal expectedAmountToBePaid = new BigDecimal("200.10");
        BigDecimal totalAmount = new BigDecimal("350.10");
        List<BigDecimal> financialHelpsAmounts = List.of(
                new BigDecimal("80.00"),
                new BigDecimal("70.00")
        );

        // When
        BigDecimal amountToBePaid = paymentCalculatorService.calculateAmountToBePaid(totalAmount, financialHelpsAmounts);

        // Then
        assertEquals(amountToBePaid, expectedAmountToBePaid);
    }

    @Test
    @DisplayName("Given a small total amount and financial help amounts, when calculateAmountToBePaid(), then throw DiscountExceedsTotalException")
    void calculateAmountToBePaidFailCase() {
        // Given
        BigDecimal totalAmount = new BigDecimal("30.00");
        List<BigDecimal> financialHelpsAmounts = List.of(
                new BigDecimal("80.00"),
                new BigDecimal("70.00")
        );

        // When
        try {
            paymentCalculatorService.calculateAmountToBePaid(totalAmount, financialHelpsAmounts);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(DiscountExceedsTotalException.class, ex.getClass());
            assertEquals("The discount total is greater than the payment amount", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given student quantity and amount to be paid, when calculateTuitionAmount(), then return expected tuition amount")
    void calculateTuitionAmountSuccessCase() {
        // Given
        int studentsQuantity = 3;
        BigDecimal amountToBePaid = new BigDecimal("487.95");
        BigDecimal expectedTuitionAmount = new BigDecimal("162.65");

        // When
        BigDecimal tuitionAmount = paymentCalculatorService.calculateTuitionAmount(amountToBePaid, studentsQuantity);

        // Then
        assertEquals(expectedTuitionAmount, tuitionAmount);
    }
}
