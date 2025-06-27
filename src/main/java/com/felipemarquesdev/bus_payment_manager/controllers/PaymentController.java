package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.*;
import com.felipemarquesdev.bus_payment_manager.services.PaymentServiceImpl;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentServiceImpl service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid PaymentRequestDTO requestBody) {
        service.create(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable(name = "id") UUID id) {
        PaymentResponseDTO responseBody = service.findById(id);
        System.out.println("CHAMOU");
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<PaymentSummaryResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResponseDTO<PaymentSummaryResponseDTO> responseBody = service.findAll(pageNumber, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping("/calculate-amounts")
    public ResponseEntity<PaymentAmountsResponseDTO> calculateAmounts(
            @RequestBody @Valid PaymentAmountsRequestDTO requestBody
    ) {
        PaymentAmountsResponseDTO responseBody = service.calculateAmounts(requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
