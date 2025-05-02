package com.felipemarquesdev.bus_payment_manager.dtos.student;

import com.felipemarquesdev.bus_payment_manager.entities.Student;
import org.springframework.data.domain.Page;

import java.util.List;

public record StudentPageResponseDTO(

        List<StudentResponseDTO> students,

        Integer pageNumber,

        Integer pageSize,

        Long totalElements,

        Integer totalPages,

        Boolean last
) {

    public static StudentPageResponseDTO fromStudentPage(Page<Student> page) {
        List<StudentResponseDTO> students = page.getContent()
                .stream()
                .map(StudentResponseDTO::fromStudent)
                .toList();

        return new StudentPageResponseDTO(
                students,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
