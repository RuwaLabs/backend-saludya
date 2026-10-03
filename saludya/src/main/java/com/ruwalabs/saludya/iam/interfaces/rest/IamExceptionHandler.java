package com.ruwalabs.saludya.iam.interfaces.rest;

import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import java.util.Map;
@RestControllerAdvice(basePackages="com.ruwalabs.saludya.iam.interfaces.rest") @Order(-10)
public class IamExceptionHandler {
    @ExceptionHandler(IamException.class) public ResponseEntity<?> domain(IamException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("code",e.getCode(),"message",e.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(Map.of("code","IAM_DUPLICATE_RECORD","message","This identity, email or guardian link is already registered"));
    }
    @ExceptionHandler(PessimisticLockingFailureException.class) public ResponseEntity<?> busy(PessimisticLockingFailureException e) {
        return ResponseEntity.status(503).body(Map.of("code","IAM_RETRY_REQUIRED","message","The account is busy. Try again later"));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<?> unreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("code","IAM_INVALID_REQUEST","message","Invalid JSON, date or account role"));
    }
}
