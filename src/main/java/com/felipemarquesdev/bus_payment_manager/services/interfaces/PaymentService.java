package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.*;
import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    void create(PaymentRequestDTO dto);

    PaymentResponseDTO findById(UUID id);

    PaymentAmountsResponseDTO calculateAmounts(PaymentAmountsRequestDTO dto);

    List<Student> getPaymentStudents(PaymentRequestDTO dto);

    PageResponseDTO<PaymentSummaryResponseDTO> findAll(int pageNumber, int pageSize);
}
