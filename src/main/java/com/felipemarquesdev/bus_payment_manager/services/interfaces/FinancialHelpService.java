package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface FinancialHelpService {

    void saveAll(Payment payment, List<FinancialHelpRequestDTO> dtos);

    List<BigDecimal> getAmountsToBeDiscounted(List<FinancialHelpRequestDTO> dtos);
}
