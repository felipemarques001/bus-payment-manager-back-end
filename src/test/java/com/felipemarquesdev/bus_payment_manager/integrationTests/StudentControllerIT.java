package com.felipemarquesdev.bus_payment_manager.integrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.*;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.repositories.StudentRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    private final String ENDPOINT = "/api/students";
    private final String STUDENT_NOT_FOUND_ERROR_MESSAGE = "Student not found with the ID provided";

    private final String USER_EMAIL = "test@gmail.com";
    private final String USER_PASSWORD = "123";

    private String authToken;

    private Student activeStudent;
    private Student inactiveStudent;

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
    @DisplayName("Given valid StudentRequestDTO, when create(), then return 201 and save student")
    void createSuccessCase() throws Exception {
        //Given
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO(
          "new-student-name",
          "33333333333",
          "major-test",
          "college-test"
        );

        // When
        mockMvc.perform(post(ENDPOINT)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isCreated());

        // Then
        Student savedStudent = studentRepository.findByPhoneNumber(studentRequestDTO.phoneNumber())
                        .orElseThrow(() -> new RuntimeException("Student not found"));

        assertEquals(3, studentRepository.count());
        assertNotNull(savedStudent.getId());
        assertTrue(savedStudent.getActive());
        assertEquals(studentRequestDTO.name(), savedStudent.getName());
        assertEquals(studentRequestDTO.phoneNumber(), savedStudent.getPhoneNumber());
        assertEquals(studentRequestDTO.major(), savedStudent.getMajor());
        assertEquals(studentRequestDTO.college(), savedStudent.getCollege());
    }

    @Test
    @DisplayName("Given phone number already in use, when create(), then return 400 and error data")
    void createFailCaseByPhoneNumberAlreadyInUse() throws Exception {
        //Given
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO(
                "new-student-name",
                activeStudent.getPhoneNumber(),
                "major-test",
                "college-test"
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.FIELD_ALREADY_IN_USE.getValue()))
                .andExpect(jsonPath("$.message").value("The phone number is already in use!"));
    }

    @Test
    @DisplayName("Given active = true in URL, when getAll(), then return Page of StudentResponseDTO with active student")
    void getAllWithActiveStudentSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "?pageNumber=0&pageSize=15&active=true";

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(15))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(activeStudent.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value(activeStudent.getName()))
                .andExpect(jsonPath("$.content[0].phoneNumber").value(activeStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.content[0].major").value(activeStudent.getMajor()))
                .andExpect(jsonPath("$.content[0].college").value(activeStudent.getCollege()))
                .andExpect(jsonPath("$.content[0].active").value(activeStudent.getActive()));
    }

    @Test
    @DisplayName("Given active = false in URL, when getAll(), then return Page of StudentResponseDTO with inactive student")
    void getAllWithInactiveStudentSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "?pageNumber=0&pageSize=15&active=false";

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(15))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(inactiveStudent.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value(inactiveStudent.getName()))
                .andExpect(jsonPath("$.content[0].phoneNumber").value(inactiveStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.content[0].major").value(inactiveStudent.getMajor()))
                .andExpect(jsonPath("$.content[0].college").value(inactiveStudent.getCollege()))
                .andExpect(jsonPath("$.content[0].active").value(inactiveStudent.getActive()));
    }

    @Test
    @DisplayName("Given valid list of StudentSummaryResponseDTO, when getAllForPayment(), then return 200 and StudentsForPaymentResponseDTO")
    void getAllForPaymentSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/for-payment";

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students", hasSize(1)))
                .andExpect(jsonPath("$.totalStudents").value(1))
                .andExpect(jsonPath("$.students[0].id").value(activeStudent.getId().toString()))
                .andExpect(jsonPath("$.students[0].name").value(activeStudent.getName()))
                .andExpect(jsonPath("$.students[0].phoneNumber").value(activeStudent.getPhoneNumber()));
    }

    @Test
    @DisplayName("Given valid student ID, when getById(), then return 200 and StudentResponseDTO")
    void getByIdSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + activeStudent.getId();

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeStudent.getId().toString()))
                .andExpect(jsonPath("$.name").value(activeStudent.getName()))
                .andExpect(jsonPath("$.phoneNumber").value(activeStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.major").value(activeStudent.getMajor()))
                .andExpect(jsonPath("$.college").value(activeStudent.getCollege()))
                .andExpect(jsonPath("$.active").value(activeStudent.getActive()));
    }

    @Test
    @DisplayName("Given invalid student ID, when getById(), then return 400 and error data")
    void getByIdFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID();

        // When and Then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given valid student id and StudentRequestDTO, when put(), then return 204 and update student")
    void putSuccessCase() throws Exception {
        //Given
        String url = ENDPOINT + "/" + activeStudent.getId();
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO(
                "new-student-name",
                "33333333333",
                "major-test",
                "college-test"
        );

        // When
        mockMvc.perform(put(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isNoContent());

        // Then
        Student updatedStudent = findStudentById(activeStudent.getId());
        assertNotNull(updatedStudent.getId());
        assertTrue(updatedStudent.getActive());
        assertEquals(studentRequestDTO.name(), updatedStudent.getName());
        assertEquals(studentRequestDTO.phoneNumber(), updatedStudent.getPhoneNumber());
        assertEquals(studentRequestDTO.major(), updatedStudent.getMajor());
        assertEquals(studentRequestDTO.college(), updatedStudent.getCollege());
    }

    @Test
    @DisplayName("Given invalid student ID, when put(), then return 400 and error data")
    void putFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID();
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO(
                "new-student-name",
                "33333333333",
                "major-test",
                "college-test"
        );

        // When and then
        mockMvc.perform(put(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given valid student ID, when delete(), then return 204 and delete")
    void deleteSuccessCase() throws Exception {
        //Given
        String url = ENDPOINT + "/" + activeStudent.getId();

        // When
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Then
        Optional<Student> student = studentRepository.findById(activeStudent.getId());
        assertTrue(student.isEmpty());
    }

    @Test
    @DisplayName("Given invalid student ID and StudentActiveRequestDTO, when delete(), then return 204")
    void deleteFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID();

        // When and then
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given valid student id and StudentRequestDTO, when patchActiveStatus(), then return 204 and update student status")
    void patchActiveStatusSuccessCase() throws Exception {
        //Given
        String url = ENDPOINT + "/" + inactiveStudent.getId() + "/active" ;
        StudentActiveRequestDTO studentActiveRequestDTO = new StudentActiveRequestDTO(true);

        // When and then
        mockMvc.perform(patch(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentActiveRequestDTO)))
                .andExpect(status().isNoContent());

        Student student = findStudentById(inactiveStudent.getId());
        assertTrue(student.getActive());
    }

    @Test
    @DisplayName("Given invalid student ID, when patchActiveStatus(), then return 400 and error data")
    void patchActiveStatusFailCase() throws Exception {
        // Given
        String url = ENDPOINT + "/" + UUID.randomUUID() + "/active";
        StudentActiveRequestDTO studentActiveRequestDTO = new StudentActiveRequestDTO(true);

        // When and then
        mockMvc.perform(patch(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentActiveRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given phone number already in use, when checkPhoneNumberExists(), then return 200 and true")
    void checkPhoneNumberExistsSuccessCaseWithPhoneNumberAlreadyInUse() throws Exception {
        // Given
        String url = ENDPOINT + "/check-phone-number/" + activeStudent.getPhoneNumber();

        // When and then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Given phone number not already in use, when checkPhoneNumberExists(), then return 200 and false")
    void checkPhoneNumberExistsSuccessCaseWithValidPhoneNumber() throws Exception {
        // Given
        String url = ENDPOINT + "/check-phone-number/55555555555";

        // When and then
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    private void deleteEntities() {
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void saveEntities() {
        activeStudent = new Student();
        activeStudent.setName("active-name-test");
        activeStudent.setPhoneNumber("11111111111");
        activeStudent.setMajor("active-major-test");
        activeStudent.setCollege("active-college-test");
        activeStudent.setActive(true);
        studentRepository.save(activeStudent);

        inactiveStudent = new Student();
        inactiveStudent.setName("inactive-name-test");
        inactiveStudent.setPhoneNumber("22222222222");
        inactiveStudent.setMajor("inactive-major-test");
        inactiveStudent.setCollege("inactive-college-test");
        inactiveStudent.setActive(false);
        studentRepository.save(inactiveStudent);

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

    private Student findStudentById(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with the ID: " + studentId));
    }
}
