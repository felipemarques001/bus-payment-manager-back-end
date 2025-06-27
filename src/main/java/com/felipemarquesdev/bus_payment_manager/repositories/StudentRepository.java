package com.felipemarquesdev.bus_payment_manager.repositories;

import com.felipemarquesdev.bus_payment_manager.entities.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Integer countByActive(boolean active);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Student> findByPhoneNumber(String phoneNumber);

    List<Student> findAllByActive(boolean active, Sort sort);

    @Query("SELECT s FROM Student s WHERE s.active = :active")
    Page<Student> findAll(Pageable pageable, boolean active);
}
