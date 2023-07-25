package ru.practicum.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;

public class ErrorHandler {

    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final ConflictException validException) {
        return conflict(validException);
    }

    private ApiError conflict(final Exception e) {
        e.printStackTrace(pw);
        return ApiError.builder()
                .errors(Collections.singletonList(sw.toString()))
                .status(HttpStatus.CONFLICT)
                .reason("Bad request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}