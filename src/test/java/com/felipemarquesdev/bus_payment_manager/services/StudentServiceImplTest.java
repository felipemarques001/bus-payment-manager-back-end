package com.felipemarquesdev.bus_payment_manager.services;


import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentsForPaymentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Student;
import com.felipemarquesdev.bus_payment_manager.exceptions.BadRequestValueException;
import com.felipemarquesdev.bus_payment_manager.exceptions.FieldAlreadyInUseException;
import com.felipemarquesdev.bus_payment_manager.exceptions.InactiveStudentException;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import com.felipemarquesdev.bus_payment_manager.repositories.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StudentServiceImplTest {

    @InjectMocks
    private StudentServiceImpl studentService;

    @Mock
    private StudentRepository studentRepository;

    private final String STUDENT_NOT_FOUND_ERROR_MESSAGE = "Student not found with the ID provided";

    private Student firstStudent;
    private Student secondStudent;
    private StudentRequestDTO studentRequestDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

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
                "33333333333",
                "student-major",
                "student-college"
        );
    }

    @Test
    @DisplayName("Given StudentRequestDTO with valid phone number, when crate(), then save with the correct student")
    void createSuccessCase() {
        // Given
        when(studentRepository.existsByPhoneNumber(studentRequestDTO.phoneNumber())).thenReturn(false);

        // When
        studentService.create(studentRequestDTO);

        // Then
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student student = captor.getValue();
        assertEquals(studentRequestDTO.name(), student.getName());
        assertEquals(studentRequestDTO.phoneNumber(), student.getPhoneNumber());
        assertEquals(studentRequestDTO.major(), student.getMajor());
        assertEquals(studentRequestDTO.college(), student.getCollege());
    }

    @Test
    @DisplayName("Given StudentRequestDTO with invalid phone number, when crate(), then throw FieldAlreadyInUseException")
    void createFailCase() {
        //Given
        when(studentRepository.existsByPhoneNumber(studentRequestDTO.phoneNumber())).thenReturn(true);

        // When
        try {
            studentService.create(studentRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(FieldAlreadyInUseException.class, ex.getClass());
            assertEquals("The phone number is already in use!", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given valid student ID, when findById(), then return StudentResponseDTO")
    void findByIdSuccessCase() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.of(firstStudent));

        // When
        StudentResponseDTO response = studentService.findById(firstStudent.getId());

        // Then
        assertEquals(firstStudent.getId(), response.id());
        assertEquals(firstStudent.getName(), response.name());
        assertEquals(firstStudent.getPhoneNumber(), response.phoneNumber());
        assertEquals(firstStudent.getMajor(), response.major());
        assertEquals(firstStudent.getCollege(), response.college());
        assertEquals(firstStudent.getActive(), response.active());
    }

    @Test
    @DisplayName("Given invalid student ID, when findById(), then throw ResourceNotFoundException")
    void findByIdFailCase() {
        //Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.empty());

        // When
        try {
            studentService.create(studentRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(STUDENT_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given valid student list, when findAll(), then return PageResponseDTO of StudentResponseDTO")
    void findAllSuccessCase() {
        // Given
        int pageNumber = 0;
        int pageSize = 15;
        Pageable expectedPageable = PageRequest.of(pageNumber, pageSize, Sort.by("name").ascending());
        Page<Student> studentPage = new PageImpl<>(List.of(firstStudent, secondStudent), expectedPageable, 2);

        when(studentRepository.findAll(expectedPageable, true)).thenReturn(studentPage);

        // When
        PageResponseDTO<StudentResponseDTO> response = studentService.findAll(pageNumber, pageSize, true);

        // Then
        assertEquals(2, response.content().size());
        assertEquals(pageNumber, response.pageNumber());
        assertEquals(pageSize, response.pageSize());
        assertEquals(2L, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(true, response.last());
        assertEquals(firstStudent.getId(), response.content().getFirst().id());
        assertEquals(firstStudent.getName(), response.content().getFirst().name());
        assertEquals(firstStudent.getPhoneNumber(), response.content().getFirst().phoneNumber());
        assertEquals(firstStudent.getMajor(), response.content().getFirst().major());
        assertEquals(firstStudent.getCollege(), response.content().getFirst().college());
        assertEquals(firstStudent.getActive(), response.content().getFirst().active());
        assertEquals(secondStudent.getId(), response.content().get(1).id());
        assertEquals(secondStudent.getName(), response.content().get(1).name());
        assertEquals(secondStudent.getPhoneNumber(), response.content().get(1).phoneNumber());
        assertEquals(secondStudent.getMajor(), response.content().get(1).major());
        assertEquals(secondStudent.getCollege(), response.content().get(1).college());
        assertEquals(secondStudent.getActive(), response.content().get(1).active());
    }

    @Test
    @DisplayName("Given valid student list, when findAllForPayment(), then return StudentsForPaymentResponseDTO")
    void findAllForPaymentSuccessCase() {
        // Given
        List<Student> students = List.of(firstStudent, secondStudent);
        when(studentRepository.findAllByActive(true, Sort.by("name"))).thenReturn(students);

        // When
        StudentsForPaymentResponseDTO response = studentService.findAllForPayment();

        // Then
        assertEquals(2, response.students().size());
        assertEquals(2, response.totalStudents());
        assertEquals(firstStudent.getId(), response.students().getFirst().id());
        assertEquals(firstStudent.getName(), response.students().getFirst().name());
        assertEquals(firstStudent.getPhoneNumber(), response.students().getFirst().phoneNumber());
        assertEquals(secondStudent.getId(), response.students().get(1).id());
        assertEquals(secondStudent.getName(), response.students().get(1).name());
        assertEquals(secondStudent.getPhoneNumber(), response.students().get(1).phoneNumber());
    }

    @Test
    @DisplayName("Given valid student ID and StudentRequestDTO, when update(), then return StudentResponseDTO")
    void updateSuccessCase() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.of(firstStudent));
        when(studentRepository.findByPhoneNumber(studentRequestDTO.phoneNumber())).thenReturn(Optional.empty());
        when(studentRepository.save(firstStudent)).thenReturn(firstStudent);

        // When
        StudentResponseDTO response = studentService.update(firstStudent.getId(), studentRequestDTO);

        // Then
        assertEquals(firstStudent.getId(), response.id());
        assertEquals(studentRequestDTO.name(), response.name());
        assertEquals(studentRequestDTO.phoneNumber(), response.phoneNumber());
        assertEquals(studentRequestDTO.major(), response.major());
        assertEquals(studentRequestDTO.college(), response.college());
        assertEquals(true, response.active());
    }

    @Test
    @DisplayName("Given invalid student ID, when update(), then throw ResourceNotFound exception")
    void updateFailCaseBecauseStudentNotFound() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.empty());
        when(studentRepository.findByPhoneNumber(studentRequestDTO.phoneNumber()))
                .thenReturn(Optional.of(secondStudent));

        // When
        try {
            studentService.update(firstStudent.getId(), studentRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(STUDENT_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given invalid phone number, when update(), then throw FieldAlreadyInUseException exception")
    void updateFailCaseBecausePhoneNumberAlreadyInUse() {
        // Given
        secondStudent.setPhoneNumber(studentRequestDTO.phoneNumber());
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.of(firstStudent));
        when(studentRepository.findByPhoneNumber(studentRequestDTO.phoneNumber()))
                .thenReturn(Optional.of(secondStudent));

        // When
        try {
            studentService.update(firstStudent.getId(), studentRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(FieldAlreadyInUseException.class, ex.getClass());
            assertEquals("The phone number is already in use!", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given valid student ID, when delete(), then delete with the correct student")
    void deleteSuccessCase() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.of(firstStudent));

        // When
        studentService.delete(firstStudent.getId());

        // Then
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).delete(captor.capture());
        Student student = captor.getValue();
        assertEquals(firstStudent.getId(), student.getId());
        assertEquals(firstStudent.getName(), student.getName());
        assertEquals(firstStudent.getPhoneNumber(), student.getPhoneNumber());
        assertEquals(firstStudent.getMajor(), student.getMajor());
        assertEquals(firstStudent.getCollege(), student.getCollege());
        assertEquals(firstStudent.getActive(), student.getActive());
    }

    @Test
    @DisplayName("Given invalid phone number, when update(), then throw FieldAlreadyInUseException exception")
    void deleteFailCase() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.empty());

        // When
        try {
            studentService.delete(firstStudent.getId());
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(STUDENT_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given active student, when findActiveStudentById(), then return the same student")
    void findActiveStudentByIdSuccessCase() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.of(firstStudent));

        // When
        Student student = studentService.findActiveStudentById(firstStudent.getId());

        // Then
        assertEquals(firstStudent.getId(), student.getId());
        assertEquals(firstStudent.getName(), student.getName());
        assertEquals(firstStudent.getPhoneNumber(), student.getPhoneNumber());
        assertEquals(firstStudent.getMajor(), student.getMajor());
        assertEquals(firstStudent.getCollege(), student.getCollege());
        assertEquals(firstStudent.getActive(), student.getActive());
    }

    @Test
    @DisplayName("Given invalid student id, when findActiveStudentById(), then throw FieldAlreadyInUseException exception")
    void findActiveStudentByIdFailCaseBecauseStudentNotFound() {
        // Given
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.empty());

        // When
        try {
            studentService.findActiveStudentById(firstStudent.getId());
        } catch (RuntimeException ex) {
            // Then
            assertEquals(ResourceNotFoundException.class, ex.getClass());
            assertEquals(STUDENT_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given non active student, when findActiveStudentById(), then throw InactiveStudentException exception")
    void findActiveStudentByStudentNotActive() {
        // Given
        String errorMessage = String.format("The student with '%s' id is inactive", firstStudent.getId());
        firstStudent.setActive(false);
        when(studentRepository.findById(firstStudent.getId())).thenReturn(Optional.of(firstStudent));

        // When
        try {
            studentService.findActiveStudentById(firstStudent.getId());
        } catch (RuntimeException ex) {
            // Then
            assertEquals(InactiveStudentException.class, ex.getClass());
            assertEquals(errorMessage, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given active student, when checkPhoneNumberExists(), then return true")
    void checkPhoneNumberExistsSuccessCase() {
        // Given
        when(studentRepository.existsByPhoneNumber(firstStudent.getPhoneNumber())).thenReturn(true);

        // When
        boolean existPhoneNumber = studentService.checkPhoneNumberExists(firstStudent.getPhoneNumber());

        // Then
        assertTrue(existPhoneNumber);
    }

    @Test
    @DisplayName("Given non active student, when findActiveStudentById(), then throw InactiveStudentException exception")
    void findActiveStudentByErrorCase() {
        // Given
        String invalidPhoneNumber = "111111";

        // When
        try {
            studentService.checkPhoneNumberExists(invalidPhoneNumber);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(BadRequestValueException.class, ex.getClass());
            assertEquals("The phone number must contain a maximum of 11 characters long", ex.getMessage());
        }
    }
}
