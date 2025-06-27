package com.felipemarquesdev.bus_payment_manager.dtos.student;

import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.UUID;

public record StudentSummaryResponseDTO (

    UUID id,

    String name,

    String phoneNumber
) {

    public static StudentSummaryResponseDTO fromStudent(Student student) {
        return new StudentSummaryResponseDTO(
          student.getId(),
          student.getName(),
          student.getPhoneNumber()
        );
    }
}
