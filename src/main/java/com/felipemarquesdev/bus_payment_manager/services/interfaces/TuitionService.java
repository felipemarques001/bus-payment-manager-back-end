package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.List;
import java.util.UUID;

public interface TuitionService {

    void saveAll(Payment payment, List<Student> students);

    void updateToPaid(UUID id, TuitionPaidRequestDTO dto);

    void updateToNotPaid(UUID id);
}
