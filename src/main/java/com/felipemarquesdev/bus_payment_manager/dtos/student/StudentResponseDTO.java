package com.felipemarquesdev.bus_payment_manager.dtos.student;

import com.felipemarquesdev.bus_payment_manager.entities.Student;

import java.util.UUID;

public record StudentResponseDTO (

        UUID id,

        String name,

        String phoneNumber,

        String major,

        String college,

        Boolean active
) {

    public static StudentResponseDTO fromStudent(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getPhoneNumber(),
                student.getMajor(),
                student.getCollege(),
                student.getActive()
        );
    }
}
