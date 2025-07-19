package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.*;
import com.felipemarquesdev.bus_payment_manager.entities.FinancialHelp;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.repositories.PaymentRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.FinancialHelpService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentCalculatorService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.StudentService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TuitionService tuitionService;

    @Mock
    private StudentService studentService;

    @Mock
    private FinancialHelpService financialHelpService;

    @Mock
    private PaymentCalculatorService paymentCalculatorService;

    private Payment payment;
    private PaymentRequestDTO paymentRequestDTO;
    private FinancialHelpRequestDTO firstFinancialHelpRequestDTO;
    private FinancialHelpRequestDTO secondFinancialHelpRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        FinancialHelp firstFinancialHelp = new FinancialHelp(
                UUID.randomUUID(),
                "first-financial-help",
                new BigDecimal("200.95"),
                null
        );
        FinancialHelp secondFinancialHelp = new FinancialHelp(
                UUID.randomUUID(),
                "second-financial-help",
                new BigDecimal("350.27"),
                null
        );

        payment = new Payment(
                UUID.randomUUID(),
                "July",
                "2025",
                new BigDecimal("3000.02"),
                new BigDecimal("2600.99"),
                new BigDecimal("600.99"),
                List.of(firstFinancialHelp, secondFinancialHelp),
                List.of(),
                LocalDateTime.now()
        );

        firstFinancialHelpRequestDTO = new FinancialHelpRequestDTO(
                "first-financial-help-request",
                firstFinancialHelp.getAmount()
        );

        secondFinancialHelpRequestDTO = new FinancialHelpRequestDTO(
                "second-financial-help-request",
                secondFinancialHelp.getAmount()
        );

        paymentRequestDTO = new PaymentRequestDTO(
                payment.getMonth(),
                payment.getYear(),
                payment.getTotalAmount(),
                List.of(firstFinancialHelpRequestDTO, secondFinancialHelpRequestDTO),
                List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        );
    }

    @Test
    @DisplayName("Given valid payment data, when create(), then save the correct payment and call saveAll() from FinancialHelpService and TuitionService")
    void createSuccessCase() {
        // Given
        List<BigDecimal> amountsToBeDiscounted = paymentRequestDTO.financialHelps()
                .stream()
                .map(FinancialHelpRequestDTO::amount)
                .toList();
        BigDecimal amountToBePaid = new BigDecimal("965.44");
        BigDecimal tuitionAmount = new BigDecimal("105.61");

        when(studentService.findActiveStudentById(any(UUID.class))).thenReturn(new Student());

        when(financialHelpService.getAmountsToBeDiscounted(paymentRequestDTO.financialHelps()))
                .thenReturn(amountsToBeDiscounted);

        when(paymentCalculatorService.calculateAmountToBePaid(paymentRequestDTO.totalAmount(), amountsToBeDiscounted))
                .thenReturn(amountToBePaid);

        when(paymentCalculatorService.calculateTuitionAmount(amountToBePaid, paymentRequestDTO.studentsIds().size()))
                .thenReturn(tuitionAmount);

        Payment savedPayment = new Payment();
        savedPayment.setId(UUID.randomUUID());
        savedPayment.setMonth("July");
        savedPayment.setYear("2025");
        savedPayment.setTotalAmount(paymentRequestDTO.totalAmount());
        savedPayment.setTotalToBePaid(amountToBePaid);
        savedPayment.setTuitionAmount(tuitionAmount);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        paymentService.create(paymentRequestDTO);

        // Then
        ArgumentCaptor<Payment> captorToPayment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captorToPayment.capture());
        Payment payment = captorToPayment.getValue();

        assertEquals(paymentRequestDTO.month(), payment.getMonth());
        assertEquals(paymentRequestDTO.year(), payment.getYear());
        assertEquals(paymentRequestDTO.totalAmount(), payment.getTotalAmount());
        assertEquals(amountToBePaid, payment.getTotalToBePaid());
        assertEquals(tuitionAmount, payment.getTuitionAmount());

        verify(financialHelpService).saveAll(savedPayment, paymentRequestDTO.financialHelps());

        ArgumentCaptor<List<Student>> studentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(tuitionService).saveAll(eq(savedPayment), studentsCaptor.capture());
        List<Student> students = studentsCaptor.getValue();

        assertEquals(paymentRequestDTO.studentsIds().size(), students.size());
    }

    @Test
    @DisplayName("Given valid payment ID, when findById(), then return PaymentResponseDTO")
    void findByIdSuccessCase() {
        // Given
        PaymentResponseDTO paymentResponseDTO = PaymentResponseDTO.fromPayment(payment);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        // When
        PaymentResponseDTO response = paymentService.findById(payment.getId());

        // Then
        assertEquals(paymentResponseDTO.id(), response.id());
        assertEquals(paymentResponseDTO.month(), response.month());
        assertEquals(paymentResponseDTO.year(), response.year());
        assertEquals(paymentResponseDTO.totalAmount(), response.totalAmount());
        assertEquals(paymentResponseDTO.totalToBePaid(), response.totalToBePaid());
        assertEquals(paymentResponseDTO.tuitionAmount(), response.tuitionAmount());

        for (int i = 0; i < paymentResponseDTO.financialHelps().size(); i++) {
            FinancialHelpResponseDTO financialHelpResponseDTO = response.financialHelps().get(i);
            FinancialHelpResponseDTO expectedFinancialHelpResponseDTO = paymentResponseDTO.financialHelps().get(i);

            assertEquals(expectedFinancialHelpResponseDTO.id(), financialHelpResponseDTO.id());
            assertEquals(expectedFinancialHelpResponseDTO.name(), financialHelpResponseDTO.name());
            assertEquals(expectedFinancialHelpResponseDTO.amount(), financialHelpResponseDTO.amount());
        }
    }

    @Test
    @DisplayName("Given invalid payment ID, when findById(), then throw ResourceNotFoundException")
    void findByIdFailCase() {
        // Given
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.empty());

        // When
        try {
            paymentService.findById(payment.getId());
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals("Payment not found with the ID provided", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given valid active students ID's, when getPaymentStudents(), then return Students")
    void getPaymentStudentsSuccessCase() {
        // Given
        when(studentService.findActiveStudentById(any(UUID.class))).thenReturn(new Student());

        // When
        List<Student> students = paymentService.getPaymentStudents(paymentRequestDTO);

        // Then
        assertEquals(2, students.size());
        assertNotNull(students.getFirst());
        assertNotNull(students.get(1));
    }

    @Test
    @DisplayName("Given valid payment page, when findAll(), then return PageResponseDTO of PaymentSummaryResponseDTO")
    void findAllSuccessCase() {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        int totalElements = 1;
        Pageable expectedPageable = PageRequest.of(pageNumber, pageSize);
        Page<Payment> studentPage = new PageImpl<>(List.of(payment), expectedPageable, totalElements);
        when(paymentRepository.findAll(expectedPageable)).thenReturn(studentPage);

        // When
        PageResponseDTO<PaymentSummaryResponseDTO> response = paymentService.findAll(pageNumber, pageSize);

        // Then
        PaymentSummaryResponseDTO paymentResponse = response.content().getFirst();
        assertEquals(pageNumber, response.pageNumber());
        assertEquals(pageSize, response.pageSize());
        assertEquals(totalElements, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.last());
        assertEquals(payment.getId(), paymentResponse.id());
        assertEquals(payment.getMonth(), paymentResponse.month());
        assertEquals(payment.getYear(), paymentResponse.year());
        assertEquals(payment.getTotalAmount(), paymentResponse.totalAmount());
        assertEquals(payment.getTuitionAmount(), paymentResponse.tuitionAmount());
    }

    @Test
    @DisplayName("")
    void calculateAmountsSuccessCase() {
        // Given
        BigDecimal totalAmount = paymentRequestDTO.totalAmount();
        int studentsQuantity = paymentRequestDTO.studentsIds().size();
        BigDecimal amountToBePaid = new BigDecimal("899.99");
        BigDecimal tuitionAmount = new BigDecimal("250.31");
        List<BigDecimal> amountsToBeDiscounted = List.of(
                firstFinancialHelpRequestDTO.amount(),
                secondFinancialHelpRequestDTO.amount()
        );
        List<FinancialHelpRequestDTO> financialHelpRequestDTOList = List.of(
                firstFinancialHelpRequestDTO,
                secondFinancialHelpRequestDTO
        );
        PaymentAmountsRequestDTO paymentAmountsRequestDTO = new PaymentAmountsRequestDTO(
                totalAmount,
                financialHelpRequestDTOList,
                studentsQuantity
        );


        when(financialHelpService.getAmountsToBeDiscounted(financialHelpRequestDTOList))
                .thenReturn(amountsToBeDiscounted);
        when(paymentCalculatorService.calculateAmountToBePaid(totalAmount, amountsToBeDiscounted))
                .thenReturn(amountToBePaid);
        when(paymentCalculatorService.calculateTuitionAmount(amountToBePaid, studentsQuantity))
                .thenReturn(tuitionAmount);

        // When
        PaymentAmountsResponseDTO response = paymentService.calculateAmounts(paymentAmountsRequestDTO);

        // Then
        assertEquals(totalAmount, response.totalAmount());
        assertEquals(amountToBePaid, response.amountToBePaid());
        assertEquals(tuitionAmount, response.tuitionAmount());
        assertEquals(studentsQuantity, response.studentsQuantity());
    }

}
