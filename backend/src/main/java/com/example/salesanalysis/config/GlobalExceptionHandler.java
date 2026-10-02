package com.example.salesanalysis.config;

import com.example.salesanalysis.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::normalizeFieldError)
                .orElse("请求参数不合法");
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleUnreadable(HttpMessageNotReadableException ex) {
        return ApiResponse.fail("JSON 请求体格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    private String normalizeFieldError(FieldError error) {
        String message = error.getDefaultMessage();
        String field = error.getField();
        if ("must not be blank".equals(message)) {
            return field + "不能为空";
        }
        if ("must not be null".equals(message)) {
            return field + "不能为空";
        }
        if (message != null && message.startsWith("must be greater than or equal to")) {
            return field + "数值不合法";
        }
        return field + ": " + message;
    }
}

