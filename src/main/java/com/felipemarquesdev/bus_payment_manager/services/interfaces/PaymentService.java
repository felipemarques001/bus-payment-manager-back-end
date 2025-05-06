package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    void create(PaymentRequestDTO dto);

    Payment save(PaymentRequestDTO dto, BigDecimal amountToBePaid, BigDecimal tuitionAmount);

    PaymentResponseDTO findById(UUID id);

    List<Student> getPaymentStudents(PaymentRequestDTO dto);
}
