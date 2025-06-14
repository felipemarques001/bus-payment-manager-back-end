package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentCalculatorService {

    BigDecimal calculateAmountToBePaid(BigDecimal totalAmount, List<BigDecimal> financialHelpAmounts);

    BigDecimal calculateTuitionAmount(BigDecimal amountToBePaid, Integer studentsQuantity);
}
