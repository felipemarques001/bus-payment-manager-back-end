package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.tuition.TuitionPaidRequestDTO;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.TuitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tuitions")
public class TuitionController {

    private final TuitionService service;

    public TuitionController(TuitionService tuitionService) {
        this.service = tuitionService;
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<Void> patchAsPaid(
            @PathVariable(name = "id") UUID id,
            @RequestBody @Valid TuitionPaidRequestDTO requestBody
    ) {
        service.updateToPaid(id, requestBody);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/not-paid")
    public ResponseEntity<Void> patchAsNotPaid(
            @PathVariable(name = "id") UUID id
    ) {
        service.updateToNotPaid(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
