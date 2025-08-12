package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentActiveRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentsForPaymentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.infra.security.SecurityConfig;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME_NAME)
@Tag(name = "StudentController", description = "Handles all endpoints for managing of students requests")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create student")
    @ApiResponse(responseCode = "201", description = "Student created with success")
    @ApiResponse(responseCode = "400", description = "Phone number already in use", content = @Content())
    ResponseEntity<Void> create(@RequestBody @Valid StudentRequestDTO requestBody) {
        service.create(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "Search for all students using pagination")
    @ApiResponse(responseCode = "200", description = "Students founded with success")
    ResponseEntity<PageResponseDTO<StudentResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(defaultValue = "true") boolean active
    ) {
        PageResponseDTO<StudentResponseDTO> responseBody = service.findAll(pageNumber, pageSize, active);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @GetMapping("/for-payment")
    @Operation(summary = "Search for all active students available for a new payment")
    @ApiResponse(responseCode = "200", description = "Active students founded with success")
    ResponseEntity<StudentsForPaymentResponseDTO> getAllForPayment() {
        StudentsForPaymentResponseDTO responseBody = service.findAllForPayment();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Search for student by ID")
    @ApiResponse(responseCode = "200", description = "Student founded with success")
    @ApiResponse(responseCode = "400", description = "Student not found by ID", content = @Content())
    ResponseEntity<StudentResponseDTO> getById(@PathVariable(name = "id") UUID id) {
        StudentResponseDTO responseBody = service.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update data of student founded by ID")
    @ApiResponse(responseCode = "200", description = "Student data update with success")
    @ApiResponse(responseCode = "400", description = "student not found by ID or phone number already in use", content = @Content())
    ResponseEntity<StudentResponseDTO> put(
            @PathVariable(name = "id") UUID id,
            @RequestBody @Valid StudentRequestDTO requestBody
    ) {
        StudentResponseDTO responseBody = service.update(id, requestBody);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseBody);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student founded by ID")
    @ApiResponse(responseCode = "200", description = "Student deleted with success")
    @ApiResponse(responseCode = "400", description = "student not found by ID", content = @Content())
    ResponseEntity<Void> delete(@PathVariable(name = "id") UUID id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Update active status of student founded by ID")
    @ApiResponse(responseCode = "200", description = "Student active status updated with success")
    @ApiResponse(responseCode = "400", description = "student not found by ID", content = @Content())
    ResponseEntity<Void> patchActiveStatus(
            @PathVariable(name = "id") UUID id,
            @RequestBody @Valid StudentActiveRequestDTO requestBody
    ) {
        service.updateActiveStatus(id, requestBody);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/check-phone-number/{phoneNumber}")
    @Operation(summary = "Checks if there is a student with the provided phone number")
    @ApiResponse(responseCode = "200", description = "Inform with there is a student with the same phone number")
    @ApiResponse(responseCode = "400", description = "Invalid phone number format", content = @Content())
    ResponseEntity<Boolean> checkPhoneNumberExists(@PathVariable(name = "phoneNumber") String phoneNumber) {
        boolean responseBody = service.checkPhoneNumberExists(phoneNumber);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
