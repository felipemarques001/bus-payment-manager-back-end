package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentSummaryResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.repositories.PaymentRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.FinancialHelpService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentCalculatorService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.StudentService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;

    private final TuitionService tuitionService;

    private final StudentService studentService;

    private final FinancialHelpService financialHelpService;

    private final PaymentCalculatorService paymentCalculatorService;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            TuitionService tuitionService,
            StudentService studentService,
            FinancialHelpService financialHelpService,
            PaymentCalculatorService paymentCalculatorService
    ) {
        this.repository = paymentRepository;
        this.tuitionService = tuitionService;
        this.studentService = studentService;
        this.financialHelpService = financialHelpService;
        this.paymentCalculatorService = paymentCalculatorService;
    }

    @Transactional
    @Override
    public void create(PaymentRequestDTO dto) {
        List<BigDecimal> amountsToBeDiscounted = financialHelpService.getAmountsToBeDiscounted(dto.financialHelps());
        BigDecimal amountToBePaid = paymentCalculatorService.calculateAmountToBePaid(dto.totalAmount(), amountsToBeDiscounted);

        List<Student> students = getPaymentStudents(dto);
        BigDecimal studentsQuantity = new BigDecimal(Integer.toString(students.size()));
        BigDecimal tuitionAmount = paymentCalculatorService.calculateTuitionAmount(amountToBePaid, studentsQuantity);

        Payment payment = save(dto, amountToBePaid, tuitionAmount);
        financialHelpService.saveAll(payment, dto.financialHelps());
        tuitionService.saveAll(payment, students);
    }

    @Transactional
    @Override
    public Payment save(PaymentRequestDTO dto, BigDecimal amountToBePaid, BigDecimal tuitionAmount) {
        Payment payment = new Payment(dto, amountToBePaid, tuitionAmount);
        return repository.save(payment);
    }

    @Override
    public PaymentResponseDTO findById(UUID id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "ID"));

        return PaymentResponseDTO.fromPayment(payment);
    }

    @Override
    public List<Student> getPaymentStudents(PaymentRequestDTO dto) {
        return dto.studentsIds()
                .stream()
                .map((id) -> studentService.findActiveStudentById(UUID.fromString(id)))
                .toList();
    }

    @Override
    public PageResponseDTO<PaymentSummaryResponseDTO> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Payment> studentsPage = repository.findAll(pageable);
        return PageResponseDTO.fromPage(studentsPage, PaymentSummaryResponseDTO::fromPayment);
    }
}
