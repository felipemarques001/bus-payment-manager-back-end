package com.felipemarquesdev.bus_payment_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.PaymentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.*;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.infra.security.TokenService;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    // We need to define these beans to prevent errors in auth token creation
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;

    private final String ENDPOINT = "/api/students";

    private final String STUDENT_NOT_FOUND_ERROR_MESSAGE = "Student not found with the ID provided";

    private String endpointWithStudentId;
    private String patchActiveStatusUrl;

    private Student firstStudent;
    private Student secondStudent;
    private StudentRequestDTO studentRequestDTO;
    private StudentActiveRequestDTO studentActiveRequestDTO;

    @BeforeEach
    void setUp() {
        firstStudent = new Student(
                UUID.randomUUID(),
                "first-student-name",
                "11111111111",
                "first-student-major",
                "first-student-college",
                true
        );

        secondStudent = new Student(
                UUID.randomUUID(),
                "second-student-name",
                "22222222222",
                "second-student-major",
                "second-student-college",
                true
        );

        studentRequestDTO = new StudentRequestDTO(
                "student-name",
                "11111111111",
                "student-major",
                "student-college"
        );

        studentActiveRequestDTO = new StudentActiveRequestDTO(true);

        endpointWithStudentId = ENDPOINT + "/" + firstStudent.getId();
        patchActiveStatusUrl = endpointWithStudentId + "/active";
    }

    @Test
    @DisplayName("Given valid StudentRequestDTO, when create(), then return 201")
    void createSuccessCase() throws Exception {
        //Given - studentRequestDTO

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Given empty fields, when create(), then return 400 and error data")
    void createFailCaseByEmptyFields() throws Exception {
        // Given
        String errorMessage = "This field cannot be empty";
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO("", "", "", "");

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(jsonPath("$.name").value(errorMessage))
                .andExpect(jsonPath("$.phoneNumber").value(errorMessage));
    }

    @Test
    @DisplayName("Given large fields, when create(), then return 400 and error data")
    void calculateAmountsFailCaseByLargeFields() throws Exception {
        // Given
        String errorMessage = "The phone number must contain a maximum of 11 characters long";
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO(
                "name-test",
                "1234567891011",
                "major-test",
                "college-test"
        );

        // When and Then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(jsonPath("$.phoneNumber").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid students, when getAll(), then return 200 and PageResponseDTO of StudentResponseDTO")
    void getAllSuccessCase() throws Exception {
        // Given
        List<StudentResponseDTO> studentResponseDTOList = List.of(
                StudentResponseDTO.fromStudent(firstStudent),
                StudentResponseDTO.fromStudent(secondStudent)
        );
        PageResponseDTO<StudentResponseDTO> pageResponseDTO = new PageResponseDTO<>(
                studentResponseDTOList,
                0,
                15,
                2L,
                1,
                true
        );

        when(studentService.findAll(0, 15, true)).thenReturn(pageResponseDTO);

        // When and Then
        mockMvc.perform(get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(15))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(firstStudent.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value(firstStudent.getName()))
                .andExpect(jsonPath("$.content[0].phoneNumber").value(firstStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.content[0].major").value(firstStudent.getMajor()))
                .andExpect(jsonPath("$.content[0].college").value(firstStudent.getCollege()))
                .andExpect(jsonPath("$.content[0].active").value(firstStudent.getActive()))
                .andExpect(jsonPath("$.content[1].id").value(secondStudent.getId().toString()))
                .andExpect(jsonPath("$.content[1].name").value(secondStudent.getName()))
                .andExpect(jsonPath("$.content[1].phoneNumber").value(secondStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.content[1].major").value(secondStudent.getMajor()))
                .andExpect(jsonPath("$.content[1].college").value(secondStudent.getCollege()))
                .andExpect(jsonPath("$.content[1].active").value(secondStudent.getActive()));
    }

    @Test
    @DisplayName("Given valid list of StudentSummaryResponseDTO, when getAllForPayment(), then return 200 and StudentsForPaymentResponseDTO")
    void getAllForPaymentSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/for-payment";
        List<StudentSummaryResponseDTO> studentResponseDTOList = List.of(
                StudentSummaryResponseDTO.fromStudent(firstStudent),
                StudentSummaryResponseDTO.fromStudent(secondStudent)
        );

        StudentsForPaymentResponseDTO studentsForPaymentResponseDTO = new StudentsForPaymentResponseDTO(
                studentResponseDTOList,
                studentResponseDTOList.size()
        );

        when(studentService.findAllForPayment()).thenReturn(studentsForPaymentResponseDTO);

        // When and Then
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students", hasSize(2)))
                .andExpect(jsonPath("$.totalStudents").value(2))
                .andExpect(jsonPath("$.students[0].id").value(firstStudent.getId().toString()))
                .andExpect(jsonPath("$.students[0].name").value(firstStudent.getName()))
                .andExpect(jsonPath("$.students[0].phoneNumber").value(firstStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.students[1].id").value(secondStudent.getId().toString()))
                .andExpect(jsonPath("$.students[1].name").value(secondStudent.getName()))
                .andExpect(jsonPath("$.students[1].phoneNumber").value(secondStudent.getPhoneNumber()));
    }

    @Test
    @DisplayName("Given valid student ID, when getById(), then return 200 and StudentResponseDTO")
    void getByIdSuccessCase() throws Exception {
        // Given
        StudentResponseDTO studentResponseDTO = StudentResponseDTO.fromStudent(firstStudent);
        when(studentService.findById(firstStudent.getId())).thenReturn(studentResponseDTO);

        // When and Then
        mockMvc.perform(get(endpointWithStudentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstStudent.getId().toString()))
                .andExpect(jsonPath("$.name").value(firstStudent.getName()))
                .andExpect(jsonPath("$.phoneNumber").value(firstStudent.getPhoneNumber()))
                .andExpect(jsonPath("$.major").value(firstStudent.getMajor()))
                .andExpect(jsonPath("$.college").value(firstStudent.getCollege()))
                .andExpect(jsonPath("$.active").value(firstStudent.getActive()));
    }

    @Test
    @DisplayName("Given invalid student ID, when getById(), then return 400 and error data")
    void getByIdFailCase() throws Exception {
        // Given
        when(studentService.findById(firstStudent.getId())).thenThrow(new ResourceNotFoundException("Student", "ID"));

        // When and then
        mockMvc.perform(get(endpointWithStudentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given valid student id and StudentRequestDTO, when put(), then return 204")
    void putSuccessCase() throws Exception {
        //Given
        StudentResponseDTO studentResponseDTO = StudentResponseDTO.fromStudent(firstStudent);
        when(studentService.update(firstStudent.getId(), studentRequestDTO)).thenReturn(studentResponseDTO);

        mockMvc.perform(put(endpointWithStudentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given invalid student ID, when put(), then return 400 and error data")
    void putFailCase() throws Exception {
        // Given
        when(studentService.update(firstStudent.getId(), studentRequestDTO)).thenThrow(new ResourceNotFoundException("Student", "ID"));

        // When and then
        mockMvc.perform(put(endpointWithStudentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given empty fields, when put(), then return 400 and error data")
    void putFailCaseByEmptyFields() throws Exception {
        // Given
        String errorMessage = "This field cannot be empty";
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO("", "", "", "");

        // When and Then
        mockMvc.perform(put(endpointWithStudentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(jsonPath("$.name").value(errorMessage))
                .andExpect(jsonPath("$.phoneNumber").value(errorMessage));
    }

    @Test
    @DisplayName("Given large fields, when put(), then return 400 and error data")
    void putAmountsFailCaseByLargeFields() throws Exception {
        // Given
        String errorMessage = "The phone number must contain a maximum of 11 characters long";
        StudentRequestDTO studentRequestDTO = new StudentRequestDTO(
                "name-test",
                "1234567891011",
                "major-test",
                "college-test"
        );

        // When and Then
        mockMvc.perform(put(endpointWithStudentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentRequestDTO)))
                .andExpect(jsonPath("$.phoneNumber").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid student ID, when delete(), then return 204")
    void deleteSuccessCase() throws Exception {
        //Given - valid student ID

        // When and then
        mockMvc.perform(delete(endpointWithStudentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given invalid student ID and StudentActiveRequestDTO, when delete(), then return 204")
    void deleteFailCase() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Student", "ID"))
                .when(studentService)
                .delete(firstStudent.getId());

        // When and then
        mockMvc.perform(delete(endpointWithStudentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given valid student id and StudentRequestDTO, when patchActiveStatus(), then return 204")
    void patchActiveStatusSuccessCase() throws Exception {
        //Given - studentActiveRequestDTO

        // When and then
        mockMvc.perform(patch(patchActiveStatusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentActiveRequestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given invalid student ID, when patchActiveStatus(), then return 400 and error data")
    void patchActiveStatusFailCase() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Student", "ID"))
                .when(studentService)
                .updateActiveStatus(firstStudent.getId(), studentActiveRequestDTO);

        // When and then
        mockMvc.perform(patch(patchActiveStatusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentActiveRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value(ErrorType.RESOURCE_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.message").value(STUDENT_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Given null fields, when patchActiveStatus(), then return 400 and error data")
    void patchActiveStatusFailCaseByNullFields() throws Exception {
        // Given
        String errorMessage = "This field cannot be null";
        StudentActiveRequestDTO studentActiveRequestDTO = new StudentActiveRequestDTO(null);

        // When and Then
        mockMvc.perform(patch(patchActiveStatusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentActiveRequestDTO)))
                .andExpect(jsonPath("$.active").value(errorMessage));
    }

    @Test
    @DisplayName("Given valid phone number, when checkPhoneNumberExists(), then return 200 and true")
    void checkPhoneNumberExistsSuccessCase() throws Exception {
        // Given
        String url = ENDPOINT + "/check-phone-number/" + firstStudent.getPhoneNumber();
        boolean hasPhoneNumber = true;
        when(studentService.checkPhoneNumberExists(firstStudent.getPhoneNumber())).thenReturn(hasPhoneNumber);

        // When and then
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasPhoneNumber));
    }
}
