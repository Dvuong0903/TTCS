package com.example.HousePrediction.exception;

import com.example.HousePrediction.dto.response.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi custom (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseObject> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ResponseObject response = new ResponseObject(
                errorCode.getStatus(),
                errorCode.getMessage(),
                null);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    // 2. Bắt lỗi validation (@NotBlank, @Min,...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject> handlingValidation(MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ResponseObject response = new ResponseObject(
                "FAILED",
                "Validation error",
                errors);

        return ResponseEntity.badRequest().body(response);
    }

    // 3. Bắt lỗi chung (tránh crash server)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject> handlingRuntimeException(Exception exception) {

        ResponseObject response = new ResponseObject(
                ErrorCode.UNCATEGORIZED_EXCEPTION.getStatus(),
                ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage() + " Chi tiết: " + exception.getMessage(),
                null);

        return ResponseEntity.internalServerError().body(response);
    }
}