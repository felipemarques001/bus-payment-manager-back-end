package com.felipemarquesdev.bus_payment_manager.repositories;

import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TuitionRepository extends JpaRepository<Tuition, UUID> { }
