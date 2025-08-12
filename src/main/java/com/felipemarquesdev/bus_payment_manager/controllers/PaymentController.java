package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.page.PageResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.payment.*;
import com.felipemarquesdev.bus_payment_manager.infra.security.SecurityConfig;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.PaymentService;
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
@RequestMapping("/api/payments")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME_NAME)
@Tag(name = "PaymentController", description = "Handles all endpoints for managing of payments requests")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create payment")
    @ApiResponse(responseCode = "201", description = "Payment created with success")
    @ApiResponse(responseCode = "400", description = "Total discounts exceed the total to be paid", content = @Content())
    public ResponseEntity<Void> create(@RequestBody @Valid PaymentRequestDTO requestBody) {
        service.create(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Search for payment by ID")
    @ApiResponse(responseCode = "200", description = "Payment founded with success")
    @ApiResponse(responseCode = "400", description = "Payment not found by ID", content = @Content())
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable(name = "id") UUID id) {
        PaymentResponseDTO responseBody = service.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @GetMapping
    @Operation(summary = "Search for all payments using pagination")
    @ApiResponse(responseCode = "200", description = "Payments founded with success")
    public ResponseEntity<PageResponseDTO<PaymentSummaryResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        PageResponseDTO<PaymentSummaryResponseDTO> responseBody = service.findAll(pageNumber, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping("/calculate-amounts")
    @Operation(summary = "Calculate payment values")
    @ApiResponse(responseCode = "200", description = "Values calculated with success")
    @ApiResponse(responseCode = "400", description = "Total discounts exceed the total to be paid", content = @Content())
    public ResponseEntity<PaymentAmountsResponseDTO> calculateAmounts(
            @RequestBody @Valid PaymentAmountsRequestDTO requestBody
    ) {
        PaymentAmountsResponseDTO responseBody = service.calculateAmounts(requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
