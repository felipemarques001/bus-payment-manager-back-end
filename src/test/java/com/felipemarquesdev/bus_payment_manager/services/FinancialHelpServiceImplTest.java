package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.FinancialHelp;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.repositories.FinancialHelpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class FinancialHelpServiceImplTest {

    @InjectMocks
    private FinancialHelpServiceImpl financialHelpService;

    @Mock
    private FinancialHelpRepository financialHelpRepository;

    private List<FinancialHelpRequestDTO> financialHelpRequestDTOList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        financialHelpRequestDTOList = List.of(
                new FinancialHelpRequestDTO("first financial help", new BigDecimal("299.39")),
                new FinancialHelpRequestDTO("second financial help", new BigDecimal("346.23"))
        );
    }

    @Test
    @DisplayName("Given list of FinancialHelpRequestDTO, when saveAll(), then save the corrects FinancialHelp")
    void saveAllSuccessCase() {
        // Given
        Payment payment = new Payment();

        // When
        financialHelpService.saveAll(payment, financialHelpRequestDTOList);

        // Then
        ArgumentCaptor<List<FinancialHelp>> captorToFinancials = ArgumentCaptor.forClass(List.class);
        verify(financialHelpRepository).saveAll(captorToFinancials.capture());
        List<FinancialHelp> financialHelpList = captorToFinancials.getValue();

        for (int i = 0; i < financialHelpList.size(); i++) {
            assertEquals(financialHelpRequestDTOList.get(i).name(), financialHelpList.get(i).getName());
            assertEquals(financialHelpRequestDTOList.get(i).amount(), financialHelpList.get(i).getAmount());
        }
    }

    @Test
    @DisplayName("Given list of FinancialHelpRequestDTO, when getAmountsToBeDiscounted(), then return list of amounts")
    void getAmountsToBeDiscounted() {
        // Given - List of FinancialHelpRequestDTO

        // When
        List<BigDecimal> amountsToBeDiscounted = financialHelpService.getAmountsToBeDiscounted(
                financialHelpRequestDTOList
        );

        // Then
        for (int i = 0; i < amountsToBeDiscounted.size(); i++) {
            assertEquals(financialHelpRequestDTOList.get(i).amount(), amountsToBeDiscounted.get(i));
        }
    }
}
