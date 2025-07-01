package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Tuition;
import com.felipemarquesdev.bus_payment_manager.enums.TuitionStatus;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tuitions")
public class TuitionController {

    private final TuitionService service;

    public TuitionController(TuitionService tuitionService) {
        this.service = tuitionService;
    }

    @GetMapping
    public ResponseEntity<List<TuitionResponseDTO>> getAllByPaymentIdAndStatus(
            @RequestParam UUID paymentId,
            @RequestParam TuitionStatus status
    ) {
        List<TuitionResponseDTO> responseBody = service.findAllByPaymentIdAndStatus(paymentId, status);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<TuitionResponseDTO> patchToPaid(
            @PathVariable(name = "id") UUID id,
            @RequestBody @Valid TuitionPaidRequestDTO requestBody
    ) {
        TuitionResponseDTO responseBody = service.updateToPaid(id, requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PatchMapping("/{id}/pending")
    public ResponseEntity<TuitionResponseDTO> patchToPending(
            @PathVariable(name = "id") UUID id
    ) {
        TuitionResponseDTO responseBody = service.updateToPending(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
