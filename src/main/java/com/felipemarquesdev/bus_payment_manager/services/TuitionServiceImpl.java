package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.repositories.TuitionRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Override
    public void updateToPaid(UUID id, TuitionPaidRequestDTO dto) {
        Tuition tuition = getTuitionById(id);
        PaymentType paymentType = PaymentType.valueOf(dto.paymentType());
        tuition.setPaymentType(paymentType);
        tuition.setIsPaid(true);
        tuition.setPaidAt(LocalDateTime.now());
        repository.save(tuition);
    }

    @Override
    public void updateToNotPaid(UUID id) {
        Tuition tuition = getTuitionById(id);
        tuition.setPaymentType(null);
        tuition.setIsPaid(false);
        tuition.setPaidAt(null);
        repository.save(tuition);
    }

    private Tuition getTuitionById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tuition", "ID"));
    }
}
