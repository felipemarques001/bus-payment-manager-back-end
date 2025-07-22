package com.felipemarquesdev.bus_payment_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.*;
import com.felipemarquesdev.bus_payment_manager.entities.FinancialHelp;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.infra.security.TokenService;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    // We need to define these beans to prevent errors in auth token creation
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;

    private final String ENDPOINT = "/api/payments";

    private Payment payment;
    private FinancialHelp financialHelp;

    @BeforeEach
    void setUp() {
        payment = new Payment(
                UUID.randomUUID(),
                "July",
                "2025",
                new BigDecimal("200.75"),
                new BigDecimal("150.25"),
                new BigDecimal("75.33"),
                new ArrayList<>(),
                new ArrayList<>(),
                LocalDateTime.now()
        );

        Tuition tuition = new Tuition();
        tuition.setId(UUID.randomUUID());

        financialHelp = new FinancialHelp();
        financialHelp.setId(UUID.randomUUID());

        payment.getFinancialHelps().add(financialHelp);
        payment.getTuitions().add(tuition);
    }

    @Test
    @DisplayName("Given valid PaymentRequestDTO, when create(), then return 201")
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
                List.of(UUID.randomUUID().toString())
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Given empty or null fields, when create(), then return 400 and error data")
    void createFailCaseByEmptyOrNullFields() throws Exception {
        // Given
        String emptyFieldErrorMessage = "This field cannot be empty";
        String nullFieldErrorMessage = "This field cannot be null";
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "",
                null
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "",
                "",
                null,
                List.of(financialHelpRequestDTO),
                List.of()
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequestDTO)))



         .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.month").value(emptyFieldErrorMessage))
                .andExpect(jsonPath("$.year").value(emptyFieldErrorMessage))
                .andExpect(jsonPath("$.totalAmount").value(nullFieldErrorMessage))
                .andExpect(jsonPath("$['financialHelps[0].name']").value(emptyFieldErrorMessage))
                .andExpect(jsonPath("$['financialHelps[0].amount']").value(nullFieldErrorMessage))
                .andExpect(jsonPath("$.studentsIds").value("must not be empty"));
    }

    @Test
    @DisplayName("Given large fields, when create(), then return 400 and error data")
    void createFailCaseByLargeFields() throws Exception {
        // Given
        String largeMonthErrorMessage = "The month must contain a maximum of 9 characters long";
        String largeYearErrorMessage = "The year must contain a maximum of 4 characters long";
        String largeBigDecimalErrorMessage = "This field must contain a maximum of 6 integers and 2 fractional digits";
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("9999999999.999")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "test-test-test",
                "test-test-test",
                new BigDecimal("9999999999.999"),
                List.of(financialHelpRequestDTO),
                List.of(UUID.randomUUID().toString())
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.month").value(largeMonthErrorMessage))
                .andExpect(jsonPath("$.year").value(largeYearErrorMessage))
                .andExpect(jsonPath("$.totalAmount").value(largeBigDecimalErrorMessage))
                .andExpect(jsonPath("$['financialHelps[0].amount']").value(largeBigDecimalErrorMessage));
    }

    @Test
    @DisplayName("Given negative fields, when create(), then return 400 and error data")
    void createFailCaseByNegativeFields() throws Exception {
        // Given
        String errorMessage = "This field must be greater than zero";
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("-1.00")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "january",
                "2005",
                new BigDecimal("-33.00"),
                List.of(financialHelpRequestDTO),
                List.of(UUID.randomUUID().toString())
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalAmount").value(errorMessage))
                .andExpect(jsonPath("$['financialHelps[0].amount']").value(errorMessage));
    }

    @Test
    @DisplayName("Given invalid UUID, when create(), then return 400 and error data")
    void createFailCaseByInvalidUUIDFields() throws Exception {
        // Given
        String errorMessage = "This field is not a valid UUID";
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("10.00")
        );

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(
                "january",
                "2005",
                new BigDecimal("33.00"),
                List.of(financialHelpRequestDTO),
                List.of("invalid-UUID")
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['studentsIds[0]']").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid payment ID, when getById(), then return 200 and PaymentResponseDTO")
    void getByIdSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + payment.getId();
        PaymentResponseDTO paymentResponseDTO = PaymentResponseDTO.fromPayment(payment);
        when(paymentService.findById(payment.getId())).thenReturn(paymentResponseDTO);

        // When and Then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.month").value(payment.getMonth()))
                .andExpect(jsonPath("$.year").value(payment.getYear()))
                .andExpect(jsonPath("$.totalAmount").value(payment.getTotalAmount()))
                .andExpect(jsonPath("$.totalToBePaid").value(payment.getTotalToBePaid()))
                .andExpect(jsonPath("$.tuitionAmount").value(payment.getTuitionAmount()))
                .andExpect(jsonPath("$.financialHelps[0].id").value(financialHelp.getId().toString()));
    }

    @Test
    @DisplayName("Given invalid payment ID, when getById(), then return 400 and error data")
    void getByIdFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + payment.getId();
        when(paymentService.findById(payment.getId())).thenThrow(new ResourceNotFoundException("Payment", "ID"));

        // When and then
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value("Payment not found with the ID provided"));
    }

    @Test
    @DisplayName("Given valid payment, when getAll(), then return 200 and PageResponseDTO of PaymentSummaryResponseDTO")
    void getAllSuccessCase() throws Exception {
        // Given
        int pageNumber = 0;
        int pageSize = 10;

        List<PaymentSummaryResponseDTO> paymentSummaryResponseDTOList = List.of(
                PaymentSummaryResponseDTO.fromPayment(payment)
        );

        PageResponseDTO<PaymentSummaryResponseDTO> pageResponseDTO = new PageResponseDTO<>(
                paymentSummaryResponseDTOList,
                pageNumber,
                pageSize,
                1L,
                1,
                true
        );

        when(paymentService.findAll(pageNumber, pageSize)).thenReturn(pageResponseDTO);

        // When and Then
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(paymentSummaryResponseDTOList.size())))
                .andExpect(jsonPath("$.pageNumber").value(pageNumber))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.totalElements").value(paymentSummaryResponseDTOList.size()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.content[0].month").value(payment.getMonth()))
                .andExpect(jsonPath("$.content[0].year").value(payment.getYear()))
                .andExpect(jsonPath("$.content[0].totalAmount").value(payment.getTotalAmount()))
                .andExpect(jsonPath("$.content[0].tuitionAmount").value(payment.getTuitionAmount()));
    }

    @Test
    @DisplayName("Given PaymentAmountsRequestDTO, when calculateAmounts(), then return PaymentAmountsResponseDTO")
    void calculateAmountsSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/calculate-amounts";
        PaymentAmountsRequestDTO paymentAmountsRequestDTO = new PaymentAmountsRequestDTO(
                new BigDecimal("1000.25"),
                List.of(),
                10
        );

        PaymentAmountsResponseDTO paymentAmountsResponseDTO = new PaymentAmountsResponseDTO(
                new BigDecimal("1000.25"),
                new BigDecimal("1000.25"),
                10,
                new BigDecimal("100.25")
        );

        when(paymentService.calculateAmounts(paymentAmountsRequestDTO)).thenReturn(paymentAmountsResponseDTO);

        // When and Then
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentAmountsRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(paymentAmountsResponseDTO.totalAmount()))
                .andExpect(jsonPath("$.amountToBePaid").value(paymentAmountsResponseDTO.amountToBePaid()))
                .andExpect(jsonPath("$.studentsQuantity").value(paymentAmountsResponseDTO.studentsQuantity()))
                .andExpect(jsonPath("$.tuitionAmount").value(paymentAmountsResponseDTO.tuitionAmount()));
    }

    @Test
    @DisplayName("Given empty or null fields, when calculateAmounts(), then return 400 and error data")
    void calculateAmountsFailCaseByEmptyOrNullFields() throws Exception {
        // Given
        String url = ENDPOINT + "/calculate-amounts";
        String nullFieldErrorMessage = "This field cannot be null";
        String emptyFieldErrorMessage = "This field cannot be empty";

        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "",
                null
        );

        PaymentAmountsRequestDTO paymentAmountsRequestDTO = new PaymentAmountsRequestDTO(
                null,
                List.of(financialHelpRequestDTO),
                null
        );

        // When and Then
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentAmountsRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalAmount").value(nullFieldErrorMessage))
                .andExpect(jsonPath("$['financialHelps[0].name']").value(emptyFieldErrorMessage))
                .andExpect(jsonPath("$['financialHelps[0].amount']").value(nullFieldErrorMessage))
                .andExpect(jsonPath("$.studentsQuantity").value(nullFieldErrorMessage));
    }

    @Test
    @DisplayName("Given large fields, when calculateAmounts(), then return 400 and error data")
    void calculateAmountsFailCaseByLargeFields() throws Exception {
        // Given
        String url = ENDPOINT + "/calculate-amounts";
        String largeBigDecimalErrorMessage = "This field must contain a maximum of 6 integers and 2 fractional digits";
        String largeStudentsQuantityErrorMessage = "This field must contain a maximum of 6 integers digits";

        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("9999999999.999")
        );

        PaymentAmountsRequestDTO paymentAmountsRequestDTO = new PaymentAmountsRequestDTO(
                new BigDecimal("9999999999.999"),
                List.of(financialHelpRequestDTO),
                100000
        );

        // When and Then
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentAmountsRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalAmount").value(largeBigDecimalErrorMessage))
                .andExpect(jsonPath("$['financialHelps[0].amount']").value(largeBigDecimalErrorMessage))
                .andExpect(jsonPath("$.studentsQuantity").value(largeStudentsQuantityErrorMessage));
    }

    @Test
    @DisplayName("Given negative fields, when calculateAmounts(), then return 400 and error data")
    void calculateAmountsFailCaseFailCaseByNegativeFields() throws Exception {
        // Given
        String url = ENDPOINT + "/calculate-amounts";
        String errorMessage = "This field must be greater than zero";
        FinancialHelpRequestDTO financialHelpRequestDTO = new FinancialHelpRequestDTO(
                "test",
                new BigDecimal("-1.00")
        );

        PaymentAmountsRequestDTO paymentAmountsRequestDTO = new PaymentAmountsRequestDTO(
                new BigDecimal("-33.00"),
                List.of(financialHelpRequestDTO),
                10
        );

        // When and Then
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentAmountsRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalAmount").value(errorMessage))
                .andExpect(jsonPath("$['financialHelps[0].amount']").value(errorMessage));
    }
}
