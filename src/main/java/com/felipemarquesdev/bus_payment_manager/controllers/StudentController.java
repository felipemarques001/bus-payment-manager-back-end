package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentPageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentResponseDTO;
import com.felipemarquesdev.bus_payment_manager.services.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<Void> create(@RequestBody @Valid StudentRequestDTO requestBody) {
        service.create(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    ResponseEntity<StudentResponseDTO> getById(@PathVariable(name = "id") UUID id) {
        StudentResponseDTO responseBody = service.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @GetMapping
    ResponseEntity<StudentPageResponseDTO> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "15") int pageSize
    ) {
        StudentPageResponseDTO responseBody = service.findAll(pageNumber, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PutMapping("/{id}")
    ResponseEntity<StudentResponseDTO> put(
            @PathVariable(name = "id") UUID id,
            @RequestBody @Valid StudentRequestDTO requestBody
    ) {
        StudentResponseDTO responseBody = service.update(id, requestBody);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseBody);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable(name = "id") UUID id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
