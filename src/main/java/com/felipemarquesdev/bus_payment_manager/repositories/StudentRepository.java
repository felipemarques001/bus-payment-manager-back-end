package com.felipemarquesdev.bus_payment_manager.repositories;

import com.felipemarquesdev.bus_payment_manager.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByPhoneNumber(String phoneNumber);
}
