package com.felipemarquesdev.bus_payment_manager.integrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.repositories.PaymentRepository;
import com.felipemarquesdev.bus_payment_manager.repositories.StudentRepository;
import com.felipemarquesdev.bus_payment_manager.repositories.TuitionRepository;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TuitionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TuitionRepository tuitionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String ENDPOINT = "/api/tuitions";

    private final String USER_EMAIL = "test@gmail.com";
    private final String USER_PASSWORD = "123";

    private String authToken;

    private Tuition tuition;
    private Student student;
    private Payment payment;

    @BeforeEach
    void setUp() throws Exception {
        saveEntities();
        getAuthToken();
    }

    @AfterEach
    void shutDown() {
        deleteEntities();
    }

    @Test
    @DisplayName("Given valid payment ID, when getAllByPaymentIdAndStatusFailCase(), then return 200 and TuitionResponseDTO")
    void getAllByPaymentIdAndStatusSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "?paymentId=" + payment.getId() + "&status=" + tuition.getStatus();

        // When and Then
        mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(tuition.getId().toString()))
                .andExpect(jsonPath("$[0].paymentType").value(tuition.getPaymentType()))
                .andExpect(jsonPath("$[0].status").value(tuition.getStatus().getValue()))
                .andExpect(jsonPath("$[0].paidAt").value(tuition.getPaidAt()))
                .andExpect(jsonPath("$[0].student.id").value(student.getId().toString()))
                .andExpect(jsonPath("$[0].student.name").value(student.getName()))
                .andExpect(jsonPath("$[0].student.phoneNumber").value(student.getPhoneNumber()))
                .andExpect(jsonPath("$[0].student.major").value(student.getMajor()))
                .andExpect(jsonPath("$[0].student.college").value(student.getCollege()))
                .andExpect(jsonPath("$[0].student.active").value(student.getActive()));
    }

    @Test
    @DisplayName("Given invalid payment ID, when getAllByPaymentIdAndStatusFailCase(), then return 400 and error data")
    void getAllByPaymentIdAndStatusFailCase() throws Exception {
        // Given
        String errorMessage = "Invalid payment ID!";
        String url = ENDPOINT + "?paymentId=" + UUID.randomUUID() + "&status=" + tuition.getStatus();

        // When and then
        mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.BAD_REQUEST_VALUE.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid tuition ID, when patchToPaid(), then update tuition, return 200 and TuitionResponseDTO")
    void patchToPaidSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + tuition.getId() + "/paid";
        PaymentType paymentType = PaymentType.PIX;
        TuitionPaidRequestDTO tuitionPaidRequestDTO = new TuitionPaidRequestDTO(paymentType.getValue());

        // When and Then
        mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tuitionPaidRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tuition.getId().toString()))
                .andExpect(jsonPath("$.paymentType").value(paymentType.getValue()))
                .andExpect(jsonPath("$.status").value(TuitionStatus.PAID.getValue()))
                .andExpect(jsonPath("$.paidAt", notNullValue()))
                .andExpect(jsonPath("$.student.id").value(student.getId().toString()))
                .andExpect(jsonPath("$.student.name").value(student.getName()))
                .andExpect(jsonPath("$.student.phoneNumber").value(student.getPhoneNumber()))
                .andExpect(jsonPath("$.student.major").value(student.getMajor()))
                .andExpect(jsonPath("$.student.college").value(student.getCollege()))
                .andExpect(jsonPath("$.student.active").value(student.getActive()));
    }

    @Test
    @DisplayName("Given invalid tuition ID, when patchToPaid(), then return 400 and error data")
    void patchToPaidFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID() + "/paid";
        PaymentType paymentType = PaymentType.PIX;
        TuitionPaidRequestDTO tuitionPaidRequestDTO = new TuitionPaidRequestDTO(paymentType.getValue());

        // When and Then
        mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + authToken)
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
        String url = ENDPOINT + "/" + tuition.getId() + "/pending";

        // When and Then
        mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tuition.getId().toString()))
                .andExpect(jsonPath("$.paymentType", nullValue()))
                .andExpect(jsonPath("$.status").value(TuitionStatus.PENDING.getValue()))
                .andExpect(jsonPath("$.paidAt", nullValue()))
                .andExpect(jsonPath("$.student.id").value(student.getId().toString()))
                .andExpect(jsonPath("$.student.name").value(student.getName()))
                .andExpect(jsonPath("$.student.phoneNumber").value(student.getPhoneNumber()))
                .andExpect(jsonPath("$.student.major").value(student.getMajor()))
                .andExpect(jsonPath("$.student.college").value(student.getCollege()))
                .andExpect(jsonPath("$.student.active").value(student.getActive()));
    }

    @Test
    @DisplayName("Given invalid tuition ID, when patchToPending(), then return 400 and error data")
    void patchToPendingFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID() + "/pending";

        // When and Then
        mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value("Tuition not found with the ID provided"));
    }

    private void deleteEntities() {
        tuitionRepository.deleteAll();
        studentRepository.deleteAll();
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void saveEntities() {
        student = new Student();
        student.setName("name-test");
        student.setPhoneNumber("11111111111");
        student.setMajor("major-test");
        student.setCollege("college-test");
        student.setActive(true);
        studentRepository.save(student);

        payment = new Payment();
        payment.setInvoiceMonth("January");
        payment.setInvoiceYear("2010");
        payment.setTotalAmount(new BigDecimal("1000.99"));
        payment.setTotalToBePaid(new BigDecimal("999.99"));
        payment.setTuitionAmount(new BigDecimal("299.99"));
        paymentRepository.save(payment);

        tuition = new Tuition();
        tuition.setStudent(student);
        tuition.setStatus(TuitionStatus.PENDING);
        tuition.setPayment(payment);
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
