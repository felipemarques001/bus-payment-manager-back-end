package com.felipemarquesdev.bus_payment_manager.repositories;

import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TuitionRepository extends JpaRepository<Tuition, UUID> {

    List<Tuition> findAllByPaymentIdAndStatus(UUID paymentId, TuitionStatus status);
}
