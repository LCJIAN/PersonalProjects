package com.lcjian.spunsugar;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lcjian.spunsugar.dto.ErrorDTO;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(UnknownResourceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    ErrorDTO handleTodoEntryNotFound(UnknownResourceException ex, Locale currentLocale) {
        return new ErrorDTO(HttpStatus.NOT_FOUND.name(), ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ErrorDTO handleTodoEntryNotFound(RuntimeException ex, Locale currentLocale) {
        ex.printStackTrace();
        return new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.name(), ex.getMessage());
    }
}
