package com.nutrifit.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleAuthException(AuthException ex, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("trace", "");
        body.put("message", ex.getMessage());

        List<Map<String, Object>> errors = new ArrayList<>();
        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("defaultMessage", ex.getMessage());
        errors.add(errorDetails);
        body.put("errors", errors);
        
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Validation (Doğrulama) hatalarını yakalar (HTTP 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();

        // Kullanıcının istediği formatta tarih
        body.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("trace", ""); // Güvenlik nedeniyle trace genelde boş bırakılır veya loglanır
        body.put("message", "Validation failed for object='" + ex.getBindingResult().getObjectName() + "'. Error count: " + ex.getBindingResult().getErrorCount());

        List<Map<String, Object>> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            Map<String, Object> errorDetails = new LinkedHashMap<>();
            if (error instanceof FieldError fieldError) {
                errorDetails.put("objectName", fieldError.getObjectName());
                errorDetails.put("field", fieldError.getField());
                errorDetails.put("rejectedValue", fieldError.getRejectedValue());
                errorDetails.put("defaultMessage", fieldError.getDefaultMessage());
                errorDetails.put("code", fieldError.getCode());
            }
            errors.add(errorDetails);
        });

        body.put("errors", errors);
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Tüm genel RuntimeException hatalarını yakalar
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Özel olarak "Bulunamadı" hataları için (404)
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNotFound(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "İstediğiniz kaynak sunucuda bulunamadı.");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}