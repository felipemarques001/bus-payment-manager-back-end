package com.felipemarquesdev.bus_payment_manager.exceptions.handler;

import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.exceptions.FieldAlreadyInUseException;
import com.felipemarquesdev.bus_payment_manager.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Map<String, String>> handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(FieldAlreadyInUseException.class)
    protected ResponseEntity<Map<String, String>> handleFieldAlreadyInUseException(FieldAlreadyInUseException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("errorType", ErrorType.FIELD_ALREADY_IN_USE.getValue());
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("errorType", ErrorType.RESOURCE_NOT_FOUND.getValue());
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
