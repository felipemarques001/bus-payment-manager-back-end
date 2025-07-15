package com.felipemarquesdev.bus_payment_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.exceptions.BadRequestValueException;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.infra.security.TokenService;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TuitionController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
class TuitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TuitionService tuitionService;

    // We need to define these beans to prevent errors in auth token creation
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;

    private final UUID PAYMENT_ID = UUID.randomUUID();

    private final UUID TUITION_ID = UUID.randomUUID();
    private final PaymentType PAYMENT_TYPE = PaymentType.PIX;
    private final TuitionStatus TUITION_STATUS = TuitionStatus.PAID;
    private final LocalDateTime PAID_AT = LocalDateTime.of(2025, 7, 15, 10, 30, 5);

    private final UUID STUDENT_ID = UUID.randomUUID();
    private final String STUDENT_NAME = "student-name";
    private final String STUDENT_PHONE_NUMBER = "11111111111";
    private final String STUDENT_MAJOR = "student-major";
    private final String STUDENT_COLLEGE = "student-college";
    private final Boolean STUDENT_ACTIVE = true;

    private final String ENDPOINT = "/api/tuitions";
    private final String GET_URL = ENDPOINT + "?paymentId=" + PAYMENT_ID + "&status=" + TUITION_STATUS;
    private final String PATCH_TO_PAID_URL = ENDPOINT + "/" + TUITION_ID + "/paid";
    private final String PATCH_TO_PENDING_URL = ENDPOINT + "/" + TUITION_ID + "/pending";

    private StudentResponseDTO studentResponseDTO;
    private TuitionResponseDTO tuitionResponseDTO;
    private TuitionPaidRequestDTO tuitionPaidRequestDTO;


    @BeforeEach
    void setUp() {
        Student student = new Student(
                STUDENT_ID,
                STUDENT_NAME,
                STUDENT_PHONE_NUMBER,
                STUDENT_MAJOR,
                STUDENT_COLLEGE,
                STUDENT_ACTIVE
        );
        studentResponseDTO = StudentResponseDTO.fromStudent(student);

        tuitionResponseDTO = new TuitionResponseDTO(
                TUITION_ID,
                PAYMENT_TYPE,
                TUITION_STATUS,
                PAID_AT,
                studentResponseDTO
        );

        tuitionPaidRequestDTO = new TuitionPaidRequestDTO(PAYMENT_TYPE.getValue());
    }

    @Test
    @DisplayName("Given valid payment ID and tuition Status, when getAllByPaymentIdAndStatusSuccessCase(), then return 200 and TuitionResponseDTO")
    void getAllByPaymentIdAndStatusSuccessCase() throws Exception {
        // Given
        when(tuitionService.findAllByPaymentIdAndStatus(PAYMENT_ID, TUITION_STATUS))
                .thenReturn(List.of(tuitionResponseDTO));

        // When and Then
        mockMvc.perform(get(GET_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TUITION_ID.toString()))
                .andExpect(jsonPath("$[0].paymentType").value(PAYMENT_TYPE.getValue()))
                .andExpect(jsonPath("$[0].status").value(TUITION_STATUS.getValue()))
                .andExpect(jsonPath("$[0].paidAt").value(PAID_AT.toString()))
                .andExpect(jsonPath("$[0].student.id").value(STUDENT_ID.toString()))
                .andExpect(jsonPath("$[0].student.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$[0].student.phoneNumber").value(STUDENT_PHONE_NUMBER))
                .andExpect(jsonPath("$[0].student.major").value(STUDENT_MAJOR))
                .andExpect(jsonPath("$[0].student.college").value(STUDENT_COLLEGE))
                .andExpect(jsonPath("$[0].student.active").value(STUDENT_ACTIVE));
    }

    @Test
    @DisplayName("Given invalid payment ID, when getAllByPaymentIdAndStatusFailCase(), then return 400 and error data")
    void getAllByPaymentIdAndStatusFailCase() throws Exception {
        // Given
        String errorMessage = "Invalid payment ID!";
        when(tuitionService.findAllByPaymentIdAndStatus(PAYMENT_ID, TUITION_STATUS))
                .thenThrow(new BadRequestValueException(errorMessage));

        // When and then
        mockMvc.perform(get(GET_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.BAD_REQUEST_VALUE.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid tuition ID, when patchToPaid(), then update tuition, return 200 and TuitionResponseDTO")
    void patchToPaidSuccessCase() throws Exception {
        // Given
        when(tuitionService.updateToPaid(TUITION_ID, tuitionPaidRequestDTO)).thenReturn(tuitionResponseDTO);

        // When and Then
        mockMvc.perform(patch(PATCH_TO_PAID_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tuitionPaidRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TUITION_ID.toString()))
                .andExpect(jsonPath("$.paymentType").value(PAYMENT_TYPE.getValue()))
                .andExpect(jsonPath("$.status").value(TUITION_STATUS.getValue()))
                .andExpect(jsonPath("$.paidAt").value(PAID_AT.toString()))
                .andExpect(jsonPath("$.student.id").value(STUDENT_ID.toString()))
                .andExpect(jsonPath("$.student.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.student.phoneNumber").value(STUDENT_PHONE_NUMBER))
                .andExpect(jsonPath("$.student.major").value(STUDENT_MAJOR))
                .andExpect(jsonPath("$.student.college").value(STUDENT_COLLEGE))
                .andExpect(jsonPath("$.student.active").value(STUDENT_ACTIVE));
    }

    @Test
    @DisplayName("Given invalid tuition ID, when patchToPaid(), then return 400 and error data")
    void patchToPaidFailCase() throws Exception {
        // Given
        when(tuitionService.updateToPaid(TUITION_ID, tuitionPaidRequestDTO))
                .thenThrow(new ResourceNotFoundException("Tuition", "ID"));

        // When and Then
        mockMvc.perform(patch(PATCH_TO_PAID_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tuitionPaidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value("Tuition not found with the ID provided"));
    }

    @Test
    @DisplayName("Given valid tuition ID, when patchToPending(), then update tuition, return 200 and TuitionResponseDTO")
    void patchToPendingSuccessCase() throws Exception {
        // Given
        tuitionResponseDTO = new TuitionResponseDTO(
                TUITION_ID,
                null,
                TuitionStatus.PENDING,
                null,
                studentResponseDTO
        );
        when(tuitionService.updateToPending(TUITION_ID)).thenReturn(tuitionResponseDTO);

        // When and Then
        mockMvc.perform(patch(PATCH_TO_PENDING_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TUITION_ID.toString()))
                .andExpect(jsonPath("$.paymentType").doesNotExist())
                .andExpect(jsonPath("$.status").value(TuitionStatus.PENDING.getValue()))
                .andExpect(jsonPath("$.paidAt").doesNotExist())
                .andExpect(jsonPath("$.student.id").value(STUDENT_ID.toString()))
                .andExpect(jsonPath("$.student.name").value(STUDENT_NAME))
                .andExpect(jsonPath("$.student.phoneNumber").value(STUDENT_PHONE_NUMBER))
                .andExpect(jsonPath("$.student.major").value(STUDENT_MAJOR))
                .andExpect(jsonPath("$.student.college").value(STUDENT_COLLEGE))
                .andExpect(jsonPath("$.student.active").value(STUDENT_ACTIVE));
    }

    @Test
    @DisplayName("Given invalid tuition ID, when patchToPending(), then return 400 and error data")
    void patchToPendingFailCase() throws Exception {
        // Given
        when(tuitionService.updateToPending(TUITION_ID)).thenThrow(new ResourceNotFoundException("Tuition", "ID"));

        // When and Then
        mockMvc.perform(patch(PATCH_TO_PENDING_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value("Tuition not found with the ID provided"));
    }
}