package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.exceptions.FieldAlreadyInUseException;
import com.felipemarquesdev.bus_payment_manager.repositories.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void create(StudentRequestDTO dto) {
        if (repository.existsByPhoneNumber(dto.phoneNumber()))
            throw new FieldAlreadyInUseException("phoneNumber");

        Student newStudent = new Student(dto);
        repository.save(newStudent);
    }
}
