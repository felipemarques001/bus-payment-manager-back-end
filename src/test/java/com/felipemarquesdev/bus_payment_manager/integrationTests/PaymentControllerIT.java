package com.felipemarquesdev.bus_payment_manager.integrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentAmountsRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.*;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TuitionRepository tuitionRepository;

    @Autowired
    private FinancialHelpRepository financialHelpRepository;

    private final String ENDPOINT = "/api/payments";
    private final String USER_EMAIL = "test@gmail.com";
    private final String USER_PASSWORD = "123";

    private Payment payment;
    private Tuition tuition;
    private Student student;
    private FinancialHelp financialHelp;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        saveEntities();
        getAuthToken();
    }

    @AfterEach
    void shutDown()  {
        deleteEntities();
    }

    @Test
    @DisplayName("Given valid PaymentRequestDTO, when create(), then return 201 and create payment, financial help and tuition")
    void createSuccessCase() throws Exception {
        // Given
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("10.00")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "July",
                "2010",
                new BigDecimal("100.00"),
                List.of(financialHelpRequestDTO),
                List.of(student.getId().toString())
        );

        // When
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isCreated());

        // Then
        Payment savedPayment = paymentRepository.findAll().getLast();
        assertNotNull(savedPayment.getId());
        assertNotNull(savedPayment.getTotalToBePaid());
        assertNotNull(savedPayment.getTuitionAmount());
        assertEquals(paymentRequestDTO.invoiceMonth(), savedPayment.getInvoiceMonth());
        assertEquals(paymentRequestDTO.invoiceYear(), savedPayment.getInvoiceYear());
        assertEquals(paymentRequestDTO.totalAmount(), savedPayment.getTotalAmount());

        FinancialHelp savedFinancialHelp = financialHelpRepository.findAll().getLast();
        assertNotNull(savedFinancialHelp.getId());
        assertEquals(financialHelpRequestDTO.name(), savedFinancialHelp.getName());
        assertEquals(financialHelpRequestDTO.amount(), savedFinancialHelp.getAmount());

        Tuition savedTuition = tuitionRepository.findAll().getLast();
        assertNotNull(savedTuition.getId());
        assertEquals(student.getId(), savedTuition.getStudent().getId());
    }

    @Test
    @DisplayName("Given a discount amount greater than total amount, when create(), then return 400 and error data")
    void createFailCaseByDiscountExceedsTotal() throws Exception {
        // Given
        String errorMessage = "The discount total is greater than the payment amount";

        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("50.00")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "July",
                "2010",
                new BigDecimal("50.00"),
                List.of(financialHelpRequestDTO, financialHelpRequestDTO),
                List.of(student.getId().toString())
        );

        // When
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.DISCOUNT_EXCEEDS_TOTAL.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given invalid student id, when create(), then return 400 and error data")
    void createFailCaseByInvalidStudentId() throws Exception {
        // Given
        String errorMessage = "Student not found with the ID provided";

        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("10.00")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "July",
                "2010",
                new BigDecimal("100.00"),
                List.of(financialHelpRequestDTO),
                List.of(UUID.randomUUID().toString())
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given inactive student id, when create(), then return 400 and error data")
    void createFailCaseByInactiveStudentId() throws Exception {
        // Given
        Student inactiveStudent = new Student();
        inactiveStudent.setName("inactive-name-test");
        inactiveStudent.setPhoneNumber("22222222222");
        inactiveStudent.setMajor("inactive-major-test");
        inactiveStudent.setCollege("inactive-college-test");
        inactiveStudent.setActive(false);
        studentRepository.save(inactiveStudent);

        String errorMessage = "The student with '" + inactiveStudent.getId() + "' id is inactive";

        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("10.00")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "July",
                "2010",
                new BigDecimal("100.00"),
                List.of(financialHelpRequestDTO),
                List.of(student.getId().toString(), inactiveStudent.getId().toString())
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.STUDENT_NOT_ACTIVE.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid payment ID, when getById(), then return 200 and payment")
    void getByIdSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + payment.getId();

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.month").value(payment.getInvoiceMonth()))
                .andExpect(jsonPath("$.year").value(payment.getInvoiceYear()))
                .andExpect(jsonPath("$.totalAmount").value(payment.getTotalAmount()))
                .andExpect(jsonPath("$.totalToBePaid").value(payment.getTotalToBePaid()))
                .andExpect(jsonPath("$.tuitionAmount").value(payment.getTuitionAmount()))
                .andExpect(jsonPath("$.financialHelps[0].id").value(financialHelp.getId().toString()));
    }

    @Test
    @DisplayName("Given invalid payment ID, when getById(), then return 400 and error data")
    void getByIdFailCaseByInvalidPaymentId() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID();
        String errorMessage = "Payment not found with the ID provided";

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid url, when getAll(), then return 200 and PageResponseDTO of PaymentSummaryResponseDTO")
    void getAllSuccessCase() throws Exception {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        String url = ENDPOINT + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize;

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageNumber").value(pageNumber))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.content[0].month").value(payment.getInvoiceMonth()))
                .andExpect(jsonPath("$.content[0].year").value(payment.getInvoiceYear()))
                .andExpect(jsonPath("$.content[0].totalAmount").value(payment.getTotalAmount()))
                .andExpect(jsonPath("$.content[0].tuitionAmount").value(payment.getTuitionAmount()));
    }

    @Test
    @DisplayName("Given valid PaymentAmountsRequestDTO, when calculateAmounts(), then return 200 and valid amounts")
    void calculateAmountsSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/calculate-amounts";
        BigDecimal expectedAmountToBePaid = new BigDecimal("300.44");
        BigDecimal expectedTuitionAmount = new BigDecimal("150.22");
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "financial-help-request-DTO-test",
                new BigDecimal("100.14")
        );
        PaymentAmountsRequestDTO paymentAmountsRequestDTO = new PaymentAmountsRequestDTO(
                new BigDecimal("400.58"),
                List.of(financialHelpRequestDTO),
                2
        );

        // When and Then
        mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentAmountsRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(paymentAmountsRequestDTO.totalAmount()))
                .andExpect(jsonPath("$.amountToBePaid").value(expectedAmountToBePaid))
                .andExpect(jsonPath("$.studentsQuantity").value(paymentAmountsRequestDTO.studentsQuantity()))
                .andExpect(jsonPath("$.tuitionAmount").value(expectedTuitionAmount));
    }

    private void deleteEntities() {
        paymentRepository.deleteAll();
        tuitionRepository.deleteAll();
        studentRepository.deleteAll();
        financialHelpRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void saveEntities() {
        payment = new Payment();
        payment.setInvoiceMonth("March");
        payment.setInvoiceYear("2020");
        payment.setTotalAmount(new BigDecimal("200.99"));
        payment.setTotalToBePaid(new BigDecimal("150.99"));
        payment.setTuitionAmount(new BigDecimal("100.99"));
        paymentRepository.save(payment);

        financialHelp = new FinancialHelp();
        financialHelp.setName("financial-help-test");
        financialHelp.setAmount(new BigDecimal("50.99"));
        financialHelp.setPayment(payment);
        financialHelpRepository.save(financialHelp);

        student = new Student();
        student.setName("active-name-test");
        student.setPhoneNumber("11111111111");
        student.setMajor("active-major-test");
        student.setCollege("active-college-test");
        student.setActive(true);
        studentRepository.save(student);

        tuition = new Tuition();
        tuition.setStatus(TuitionStatus.PENDING);
        tuition.setPayment(payment);
        tuition.setStudent(student);
        tuitionRepository.save(tuition);

        User user = new User();
        user.setEmail(USER_EMAIL);
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        userRepository.save(user);
    }

    private void getAuthToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseContent);
        authToken = jsonNode.get("token").asText();
    }
}
