package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    void create(PaymentRequestDTO dto);

    Payment save(PaymentRequestDTO dto, BigDecimal amountToBePaid, BigDecimal tuitionAmount);

    List<Student> getPaymentStudents(PaymentRequestDTO dto);
}
