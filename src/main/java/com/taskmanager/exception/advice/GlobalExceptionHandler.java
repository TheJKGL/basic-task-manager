package com.taskmanager.exception.advice;

import com.taskmanager.model.ErrorResponse;
import com.taskmanager.exception.DuplicationException;
import com.taskmanager.exception.PatcherServiceException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnmodifiedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ErrorResponse(null, errors, request.getServletPath());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleValidationException(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return new ErrorResponse(exception.getMessage(), null, request.getServletPath());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    protected ErrorResponse handleException(ResourceNotFoundException e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), null, request.getServletPath());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {UnmodifiedException.class})
    protected ErrorResponse handleException(UnmodifiedException e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), null, request.getServletPath());
    }

    @ExceptionHandler(value = {DuplicationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ErrorResponse handleException(DuplicationException e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), null, request.getServletPath());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {PatcherServiceException.class})
    protected ErrorResponse handleException(PatcherServiceException e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), null, request.getServletPath());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ErrorResponse handleException(Exception e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), null, request.getServletPath());
    }
}