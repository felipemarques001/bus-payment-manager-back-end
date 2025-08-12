package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.infra.security.SecurityConfig;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tuitions")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME_NAME)
@Tag(name = "TuitionController", description = "Handles all endpoints for managing of student tuition payments requests")
public class TuitionController {

    private final TuitionService service;

    public TuitionController(TuitionService tuitionService) {
        this.service = tuitionService;
    }

    @GetMapping
    @Operation(summary = "Search for all tuition by payment ID and status")
    @ApiResponse(responseCode = "200", description = "Tuition founded with success")
    @ApiResponse(responseCode = "400", description = "Invalid payment ID", content = @Content())
    public ResponseEntity<List<TuitionResponseDTO>> getAllByPaymentIdAndStatus(
            @RequestParam UUID paymentId,
            @RequestParam TuitionStatus status
    ) {
        List<TuitionResponseDTO> responseBody = service.findAllByPaymentIdAndStatus(paymentId, status);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PatchMapping("/{id}/paid")
    @Operation(summary = "Update tuition status to paid")
    @ApiResponse(responseCode = "200", description = "Tuition status updated with success")
    @ApiResponse(responseCode = "400", description = "Tuition not found by ID", content = @Content())
    public ResponseEntity<TuitionResponseDTO> patchToPaid(
            @PathVariable(name = "id") UUID id,
            @RequestBody @Valid TuitionPaidRequestDTO requestBody
    ) {
        TuitionResponseDTO responseBody = service.updateToPaid(id, requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PatchMapping("/{id}/pending")
    @Operation(summary = "Update tuition status to pending")
    @ApiResponse(responseCode = "200", description = "Tuition status updated with success")
    @ApiResponse(responseCode = "400", description = "Tuition not found by ID", content = @Content())
    public ResponseEntity<TuitionResponseDTO> patchToPending(
            @PathVariable(name = "id") UUID id
    ) {
        TuitionResponseDTO responseBody = service.updateToPending(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
