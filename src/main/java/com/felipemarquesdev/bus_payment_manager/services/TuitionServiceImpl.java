package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.repositories.TuitionRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TuitionServiceImpl implements TuitionService {

    private final TuitionRepository repository;

    public TuitionServiceImpl(TuitionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public void saveAll(Payment payment, List<Student> students) {
        List<Tuition> tuitionList = students.stream()
                .map((student -> new Tuition(payment, student)))
                .toList();

        repository.saveAll(tuitionList);
    }
}
