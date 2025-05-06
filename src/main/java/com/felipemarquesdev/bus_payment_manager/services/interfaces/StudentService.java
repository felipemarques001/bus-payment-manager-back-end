package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentActiveRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.UUID;

public interface StudentService {

    void save(StudentRequestDTO dto);

    StudentResponseDTO findById(UUID id);

    PageResponseDTO<StudentResponseDTO> findAll(int pageNumber, int pageSize);

    StudentResponseDTO update(UUID id, StudentRequestDTO dto);

    void delete(UUID id);

    void updateActiveStatus(UUID id, StudentActiveRequestDTO dto);

    Student findActiveStudentById(UUID id);
}
