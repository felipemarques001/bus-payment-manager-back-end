package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.List;

public interface TuitionService {

    void saveAll(Payment payment, List<Student> students);
}
