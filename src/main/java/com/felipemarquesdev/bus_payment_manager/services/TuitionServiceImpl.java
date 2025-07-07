package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.exceptions.BadRequestValueException;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.repositories.TuitionRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TuitionServiceImpl implements TuitionService {

    private final TuitionRepository repository;

    public TuitionServiceImpl(TuitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TuitionResponseDTO> findAllByPaymentIdAndStatus(UUID paymentId, TuitionStatus status) {
        List<Tuition> tuitionList = repository.findAllByPaymentIdAndStatus(paymentId, status)
                .stream()
                .sorted(Comparator.comparing(tuition -> tuition.getStudent().getName()))
                .toList();

        if (tuitionList.isEmpty()) {
            throw new BadRequestValueException("Invalid payment ID!");
        }

        return tuitionList.stream()
                .map((TuitionResponseDTO::fromTuition))
                .toList();
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
    public TuitionResponseDTO updateToPaid(UUID id, TuitionPaidRequestDTO dto) {
        Tuition tuition = getTuitionById(id);
        PaymentType paymentType = PaymentType.valueOf(dto.paymentType());
        tuition.setPaymentType(paymentType);
        tuition.setStatus(TuitionStatus.PAID);
        tuition.setPaidAt(LocalDateTime.now());
        Tuition updatedTuition = repository.save(tuition);
        return TuitionResponseDTO.fromTuition(updatedTuition);
    }

    @Override
    public TuitionResponseDTO updateToPending(UUID id) {
        Tuition tuition = getTuitionById(id);
        tuition.setPaymentType(null);
        tuition.setStatus(TuitionStatus.PENDING);
        tuition.setPaidAt(null);
        Tuition updatedTuition = repository.save(tuition);
        return TuitionResponseDTO.fromTuition(updatedTuition);
    }

    private Tuition getTuitionById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tuition", "ID"));
    }
}
