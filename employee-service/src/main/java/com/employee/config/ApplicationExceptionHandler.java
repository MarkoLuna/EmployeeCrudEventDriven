package com.employee.config;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.InvalidDataException;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;

/**
 * Application exception handler.
 */
@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String RESPONSE_ENTITY_ERROR_HEADER = "app-context-error";

    /**
     * Exception to be thrown when validation on an argument annotated with @Valid fails.
     *
     * @param ex Exception object
     * @param headers response headers
     * @param status response status
     * @param request current request
     * @return {@link ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        String error = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return responseEntity(error, status);
    }

    /**
     * Exception to be thrown when validation on an enum argument annotated with @Valid fails.
     *
     * @param ex Exception object
     * @param headers response headers
     * @param status response status
     * @param request current request
     * @return {@link ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        String message = ex != null && StringUtils.isNotEmpty(ex.getMessage())
                ? ex.getMessage().replace("\r", "").replace("\n", "") : "";
        return responseEntity(message, status);
    }

    /**
     * catches exception that indicates a missing parameter.
     *
     * @param ex Exception object
     * @param headers response headers
     * @param status response status
     * @param request current request
     * @return response entity instance
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responseEntity(ex.getMessage(), status);
    }

    /**
     * catches when we want to treat binding exceptions as unrecoverable.
     *
     * @param ex Exception object
     * @param headers response headers
     * @param status response status
     * @param request current request
     * @return response entity instance
     */
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responseEntity(ex.getMessage(), status);
    }

    private ResponseEntity<Object> responseEntity(String responseMessage, HttpStatusCode httpStatus) {
        log.warn("Exception '{}' status '{}'", responseMessage, httpStatus.value());
        return ResponseEntity
                .status(httpStatus)
                .headers(httpHeaders(responseMessage))
                .build();
    }

    private HttpHeaders httpHeaders(String responseMessage) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.set(RESPONSE_ENTITY_ERROR_HEADER, responseMessage);
        return httpHeaders;
    }

    /**
     * Map an exception to a response entity.
     *
     * @param ex Handled exception.
     * @return ResponseEntity for the exception.
     */
    @ExceptionHandler({ConstraintViolationException.class, ValidationException.class})
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        return responseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Implement a handler for a {@link AccessDeniedException}.
     * @param ex the {@link AccessDeniedException}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return responseEntity(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Implement a handler for a {@link Exception}.
     * @param ex the {@link Exception}
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return responseEntity(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmployeeNotFound.class)
    public final ResponseEntity<Object> handleEmployeeNotFoundException(EmployeeNotFound ex,
                                                                        WebRequest request) {
        return responseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidDataException.class)
    public final ResponseEntity<Object> handleInvalidDataException(InvalidDataException ex,
                                                                   WebRequest request) {
        return responseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}