package com.cnom.orderflow.exception;

import com.cnom.orderflow.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> duplicateEmailError(DuplicateEmailException err) {
        return new ResponseEntity<>(new ErrorResponse(err.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> productNotFoundError(ProductNotFoundException err){
        return new ResponseEntity<>(new ErrorResponse(err.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> inventoryNotFoundException(InventoryNotFoundException err) {
        return new ResponseEntity<>(new ErrorResponse(err.getMessage()), HttpStatus.NOT_FOUND);
    }
}
