package com.example.lab1.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        if (isApiRequest(request)) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        ModelAndView mav = new ModelAndView(resolveWebView(request));
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("error", "Validation failed");
        mav.addObject("validationErrors", errors);
        return mav;
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntime(RuntimeException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }

        ModelAndView mav = new ModelAndView(resolveWebView(request));
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("error", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }

        ModelAndView mav = new ModelAndView(resolveWebView(request));
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("error", "Internal server error");
        return mav;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().startsWith("/api/");
    }

    private String resolveWebView(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/register")) {
            return "register";
        }
        return "login";
    }
}

