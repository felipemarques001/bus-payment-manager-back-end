package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.FinancialHelp;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.repositories.FinancialHelpRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.FinancialHelpService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialHelpImpl implements FinancialHelpService {

    private final FinancialHelpRepository repository;

    public FinancialHelpImpl(FinancialHelpRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public void saveAll(Payment payment, List<FinancialHelpRequestDTO> dtos) {
        List<FinancialHelp> financialHelpList = dtos.stream()
                .map((dto) -> new FinancialHelp(dto.name(), dto.amount(), payment))
                .toList();

        repository.saveAll(financialHelpList);
    }

    @Override
    public List<BigDecimal> getAmountsToBeDiscounted(List<FinancialHelpRequestDTO> dtos) {
        return dtos.stream()
                .map(FinancialHelpRequestDTO::amount)
                .toList();
    }
}
