package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentActiveRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.exceptions.FieldAlreadyInUseException;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.exceptions.InactiveStudentException;
import com.felipemarquesdev.bus_payment_manager.repositories.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void create(StudentRequestDTO dto) {
        if (repository.existsByPhoneNumber(dto.phoneNumber()))
            throw new FieldAlreadyInUseException("phone number");

        Student newStudent = new Student(dto);
        repository.save(newStudent);
    }

    public StudentResponseDTO findById(UUID id) {
        Student student = findStudentById(id);
        return StudentResponseDTO.fromStudent(student);
    }

    public PageResponseDTO<StudentResponseDTO> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Student> studentsPage = repository.findAllActive(pageable);
        return PageResponseDTO.fromPage(studentsPage, StudentResponseDTO::fromStudent);
    }

    @Transactional
    public StudentResponseDTO update(UUID id, StudentRequestDTO dto) {
        Student student = findStudentById(id);

        Optional<Student> studentFoundedByPhoneNumber = repository.findByPhoneNumber(dto.phoneNumber());
        if (studentFoundedByPhoneNumber.isPresent() && studentFoundedByPhoneNumber.get().getId() != id)
            throw new FieldAlreadyInUseException("phone number");

        student.setName(dto.name());
        student.setPhoneNumber(dto.phoneNumber());
        student.setMajor(dto.major());
        student.setCollege(dto.college());

        Student updatedStudent = repository.save(student);
        return StudentResponseDTO.fromStudent(updatedStudent);
    }

    @Transactional
    public void delete(UUID id) {
        Student student = findStudentById(id);
        repository.delete(student);
    }

    @Transactional
    public void updateActiveStatus(UUID id, StudentActiveRequestDTO dto) {
        Student student = findStudentById(id);
        student.setActive(dto.active());
        repository.save(student);
    }

    public Student findActiveStudentById(UUID id) {
        Student student = findStudentById(id);
        if (!student.getActive())
            throw new InactiveStudentException(id);

        return student;
    }

    private Student findStudentById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "ID"));
    }
}
