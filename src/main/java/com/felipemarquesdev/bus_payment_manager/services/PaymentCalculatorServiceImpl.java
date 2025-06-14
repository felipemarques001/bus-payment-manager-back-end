package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.exceptions.DiscountExceedsTotalException;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {

    @Override
    public BigDecimal calculateAmountToBePaid(BigDecimal totalAmount, List<BigDecimal> financialHelpAmounts) {
        BigDecimal financialHelpAmountTotal = financialHelpAmounts
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (financialHelpAmountTotal.compareTo(totalAmount) > 0)
            throw new DiscountExceedsTotalException();

        return totalAmount.subtract(financialHelpAmountTotal);
    }

    @Override
    public BigDecimal calculateTuitionAmount(BigDecimal amountToBePaid, Integer studentsQuantity) {
        BigDecimal studentsQuantityAsBigDecimal = new BigDecimal(Integer.toString(studentsQuantity));
        return amountToBePaid.divide(studentsQuantityAsBigDecimal, 2, RoundingMode.UP);
    }
}
