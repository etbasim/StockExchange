package com.stockexchange.casestudy;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GeneralExceptionHandler {

    // TODO enhance body message with request information and create builder for response body

    //Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("status", HttpStatus.BAD_REQUEST.value());
        bodyMap.put("message", ex.getMessage());

        return new ResponseEntity<>(bodyMap, HttpStatus.BAD_REQUEST);
    }

    //Handle validation exceptions
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("status", HttpStatus.BAD_REQUEST.value());
        bodyMap.put("message", ex.getMessage());

        return new ResponseEntity<>(bodyMap, HttpStatus.BAD_REQUEST);
    }

    // Default exception handler for rest
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleException(Throwable throwable, WebRequest request) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        bodyMap.put("message", throwable.getMessage());

        return new ResponseEntity<>(bodyMap, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
