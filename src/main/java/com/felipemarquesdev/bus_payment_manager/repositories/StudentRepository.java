package com.felipemarquesdev.bus_payment_manager.repositories;

import com.felipemarquesdev.bus_payment_manager.entities.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Student> findByPhoneNumber(String phoneNumber);

    @Query("SELECT s FROM Student s WHERE s.active = true")
    Page<Student> findAllActive(Pageable pageable);
}
