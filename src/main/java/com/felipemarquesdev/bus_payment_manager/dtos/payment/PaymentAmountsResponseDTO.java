package com.felipemarquesdev.bus_payment_manager.dtos.payment;

import java.math.BigDecimal;

public record PaymentAmountsResponseDTO(

        BigDecimal totalAmount,

        BigDecimal amountToBePaid,

        Integer studentsQuantity,

        BigDecimal tuitionAmount
) { }
