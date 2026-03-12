package com.smartserve.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice

public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleMyErrors(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error_message", ex.getMessage());
        response.put("status", "400");

        return ResponseEntity.badRequest().body(response);
    }

}
