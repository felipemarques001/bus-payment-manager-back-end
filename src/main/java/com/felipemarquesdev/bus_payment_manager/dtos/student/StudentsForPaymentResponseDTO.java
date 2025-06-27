package com.felipemarquesdev.bus_payment_manager.dtos.student;

import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.List;

public record StudentsForPaymentResponseDTO (

        List<StudentSummaryResponseDTO> students,

        Integer totalStudents
) {

    public static StudentsForPaymentResponseDTO fromStudents(List<Student> students) {
        List<StudentSummaryResponseDTO> studentsDto = students.stream()
                .map((StudentSummaryResponseDTO::fromStudent))
                .toList();

        return new StudentsForPaymentResponseDTO(
                studentsDto,
                studentsDto.size()
        );
    }
}
