package com.felipemarquesdev.bus_payment_manager.dtos.tuition;

import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;

import java.util.UUID;

public record TuitionResponseDTO(

        UUID id,

        PaymentType paymentType,

        Boolean isPaid,

        StudentResponseDTO student
) {

    public static TuitionResponseDTO fromTuition(Tuition tuition) {
        return new TuitionResponseDTO(
                tuition.getId(),
                tuition.getPaymentType(),
                tuition.getIsPaid(),
                StudentResponseDTO.fromStudent(tuition.getStudent())
        );
    }
}
