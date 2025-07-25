package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.exceptions.BadRequestValueException;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.repositories.TuitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TuitionServiceImplTest {

    @InjectMocks
    private TuitionServiceImpl tuitionService;

    @Mock
    private TuitionRepository tuitionRepository;

    private final UUID PAYMENT_ID = UUID.randomUUID();
    private final UUID STUDENT_ID = UUID.randomUUID();

    private final UUID TUITION_ID = UUID.randomUUID();
    private final PaymentType PAYMENT_TYPE = PaymentType.PIX;
    private final TuitionStatus TUITION_STATUS = TuitionStatus.PAID;
    private final LocalDateTime PAID_AT = LocalDateTime.of(2025, 7, 15, 10, 30, 5);

    private final String TUITION_NOT_FOUND_ERROR_MESSAGE = "Tuition not found with the ID provided";

    private Tuition tuition;
    private Student student;
    private Payment payment;
    private TuitionPaidRequestDTO tuitionPaidRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        student = new Student();
        student.setId(STUDENT_ID);

        payment = new Payment();
        payment.setId(PAYMENT_ID);

        tuition = new Tuition(
                TUITION_ID,
                PAYMENT_TYPE,
                TUITION_STATUS,
                PAID_AT,
                payment,
                student
        );

        tuitionPaidRequestDTO = new TuitionPaidRequestDTO(PAYMENT_TYPE.getValue());
    }

    @Test
    @DisplayName("Given valid tuition list, when findAllByPaymentIdAndStatus(), then return TuitionResponseDTO list")
    void findAllByPaymentIdAndStatusSuccessCase() {
        // Given
        when(tuitionRepository.findAllByPaymentId(PAYMENT_ID))
                .thenReturn(List.of(tuition));

        // When
        List<TuitionResponseDTO> response = tuitionService.findAllByPaymentIdAndStatus(PAYMENT_ID, TUITION_STATUS);

        // Then
        assertEquals(1, response.size());
        assertEquals(TUITION_ID, response.getFirst().id());
        assertEquals(PAYMENT_TYPE, response.getFirst().paymentType());
        assertEquals(TUITION_STATUS, response.getFirst().status());
        assertEquals(PAID_AT, response.getFirst().paidAt());
        assertEquals(STUDENT_ID, response.getFirst().student().id());
    }

    @Test
    @DisplayName("Given empty tuition list, when findAllByPaymentIdAndStatus(), then throw BadRequestValueException")
    void findAllByPaymentIdAndStatusFailCase() {
        // Given
        when(tuitionRepository.findAllByPaymentId(PAYMENT_ID)).thenReturn(List.of());

        // When
        try {
            tuitionService.findAllByPaymentIdAndStatus(PAYMENT_ID, TUITION_STATUS);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(BadRequestValueException.class, ex.getClass());
            assertEquals("Invalid payment ID!", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given valid students and payment, when saveAll(), then save the tuition list")
    void saveAllSuccessCase() {
        // Given
        UUID secondStudentId = UUID.randomUUID();
        Student secondStudent = new Student();
        secondStudent.setId(secondStudentId);
        List<Student> students = List.of(student, secondStudent);

        // When
        tuitionService.saveAll(payment, students);

        // Then
        ArgumentCaptor<List<Tuition>> captor = ArgumentCaptor.forClass(List.class);
        verify(tuitionRepository).saveAll(captor.capture());
        List<Tuition> tuitionList = captor.getValue();

        assertEquals(2, tuitionList.size());
        assertEquals(PAYMENT_ID, tuitionList.get(0).getPayment().getId());
        assertEquals(STUDENT_ID, tuitionList.get(0).getStudent().getId());
        assertEquals(PAYMENT_ID, tuitionList.get(1).getPayment().getId());
        assertEquals(secondStudentId, tuitionList.get(1).getStudent().getId());
    }

    @Test
    @DisplayName("Given valid tuition id and TuitionPaidRequestDTO, when updateToPaid(), then update status and return TuitionResponseDTO")
    void updateToPaidSuccessCase() {
        // Given
        tuition.setStatus(TuitionStatus.PENDING);
        tuition.setPaymentType(null);
        tuition.setPaidAt(null);

        LocalDateTime dateTimeBeforeCall = LocalDateTime.now();

        when(tuitionRepository.findById(TUITION_ID)).thenReturn(Optional.of(tuition));
        when(tuitionRepository.save(tuition)).thenReturn(tuition);

        // When
        TuitionResponseDTO response = tuitionService.updateToPaid(TUITION_ID, tuitionPaidRequestDTO);

        // Then
        LocalDateTime dateTimeAfterCall = LocalDateTime.now();
        assertEquals(TUITION_ID, response.id());
        assertEquals(PAYMENT_TYPE, response.paymentType());
        assertEquals(TuitionStatus.PAID, response.status());
        assertFalse(response.paidAt().isBefore(dateTimeBeforeCall));
        assertFalse(response.paidAt().isAfter(dateTimeAfterCall));
        assertEquals(STUDENT_ID, response.student().id());
    }

    @Test
    @DisplayName("Given invalid tuition id, when updateToPaid(), then throw ResourceNotFoundException")
    void updateToPaidFailCase() {
        // Given
        when(tuitionRepository.findById(TUITION_ID)).thenReturn(Optional.empty());
        when(tuitionRepository.save(tuition)).thenReturn(tuition);

        // When
        try {
            tuitionService.updateToPaid(PAYMENT_ID, tuitionPaidRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(TUITION_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given valid tuition id, when updateToPending(), then update status and return TuitionResponseDTO")
    void updateToPendingSuccessCase() {
        // Given
        when(tuitionRepository.findById(TUITION_ID)).thenReturn(Optional.of(tuition));
        when(tuitionRepository.save(tuition)).thenReturn(tuition);

        // When
        TuitionResponseDTO response = tuitionService.updateToPending(TUITION_ID);

        // Then
        assertNull(response.paidAt());
        assertNull(response.paymentType());
        assertEquals(TUITION_ID, response.id());
        assertEquals(STUDENT_ID, response.student().id());
        assertEquals(TuitionStatus.PENDING, response.status());
    }

    @Test
    @DisplayName("Given invalid tuition id, when updateToPending(), then throw ResourceNotFoundException")
    void updateToPendingFailCase() {
        // Given
        when(tuitionRepository.findById(TUITION_ID)).thenReturn(Optional.empty());
        when(tuitionRepository.save(tuition)).thenReturn(tuition);

        // When
        try {
            tuitionService.updateToPending(PAYMENT_ID);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(TUITION_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
        }
    }
}
