package com.dbtraining.reconx.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * ============================================================================
 * GlobalExceptionHandler
 *
 * WHAT:    Centralized exception handler for the ReconX REST API.
 * HOW:     Uses Spring's {@link RestControllerAdvice} and
 *          {@link ExceptionHandler} methods to translate exceptions into
 *          RFC 7807 {@link ProblemDetail} responses with appropriate HTTP
 *          status codes.
 * WHY:     Centralizing exception handling ensures that all API endpoints
 *          return consistent, structured error responses without duplicating
 *          error-handling logic in individual controllers.
 * ============================================================================
 */

/**
 * ============================================================================
 * TICKET-ADV062 — RFC 7807 ProblemDetail for every ReconException
 *
 * Maps each domain exception subtype to the right HTTP status, with a
 * structured ProblemDetail body so clients don't have to parse free text.
 * ============================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeNotFoundException.class)
    public ProblemDetail notFound(TradeNotFoundException ex) {
        // TODO(TICKET-ADV062): return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(DuplicateTradeRefException.class)
    public ProblemDetail duplicate(DuplicateTradeRefException ex) {
        // TODO(TICKET-ADV062): map DuplicateTradeRefException -> HttpStatus.CONFLICT (409).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(InvalidTradeException.class)
    public ProblemDetail invalid(InvalidTradeException ex) {
        // TODO(TICKET-ADV062): map InvalidTradeException -> HttpStatus.BAD_REQUEST (400).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(ReconciliationMismatchException.class)
    public ProblemDetail mismatch(ReconciliationMismatchException ex) {
        // TODO(TICKET-ADV062): map ReconciliationMismatchException -> HttpStatus.UNPROCESSABLE_ENTITY (422).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        // TODO(TICKET-ADV062): join field errors ("field: message; ...") and return BAD_REQUEST ProblemDetail.
        //   Hint: ex.getBindingResult().getFieldErrors().stream().map(...).collect(Collectors.joining("; "))
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail constraint(ConstraintViolationException ex) {
        // TODO(TICKET-ADV062): map ConstraintViolationException -> HttpStatus.BAD_REQUEST (400).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }
}
