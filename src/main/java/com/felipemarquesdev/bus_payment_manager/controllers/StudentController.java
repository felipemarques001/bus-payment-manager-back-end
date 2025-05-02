package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.student.StudentRequestDTO;
import com.felipemarquesdev.bus_payment_manager.services.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
