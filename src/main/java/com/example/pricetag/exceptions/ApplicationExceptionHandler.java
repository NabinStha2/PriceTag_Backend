package com.example.pricetag.exceptions;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.utils.ColorLogger;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApplicationExceptionHandler {

    // Handle custom ApplicationException
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleApplicationException(
            final ApplicationException exception, final HttpServletRequest request) {
        return new ResponseEntity<>(
                buildErrorResponse(exception.getMessage(), exception.getHttpStatus(), exception,
                                   request), exception.getHttpStatus());
    }

    // Handle JWT Expired Exception
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleExpiredJwtException(
            final ExpiredJwtException exception, final HttpServletRequest request) {
        return new ResponseEntity<>(buildErrorResponse("JWT token has expired. Please login again.",
                                                       HttpStatus.UNAUTHORIZED, exception, request),
                                    HttpStatus.UNAUTHORIZED);
    }

    // Handle unknown exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponseDto<Void>> handleUnknownException(final Exception exception,
                                                                          final HttpServletRequest request) {
        return new ResponseEntity<>(buildErrorResponse(
//                "An unexpected error occurred. Please contact support.",
                exception.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR, exception,
                request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException exception, final HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(
                        error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        return new ResponseEntity<>(buildValidationErrorResponse(
                "Validation failed for the request. Please check the errors and try again.",
                HttpStatus.BAD_REQUEST, validationErrors, exception, request),
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleDataAccessException(
            DataAccessException exception, HttpServletRequest request) {

        return new ResponseEntity<>(
                buildErrorResponse("A database operation failed. Please try again.",
                                   HttpStatus.INTERNAL_SERVER_ERROR, exception, request),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private CommonResponseDto<Void> buildErrorResponse(String message, HttpStatus status,
                                                       Exception exception,
                                                       HttpServletRequest request) {
        String guid = UUID
                .randomUUID()
                .toString();
        ColorLogger.logError(String.format("%s :: GUID=%s; message=%s", exception
                .getClass()
                .getSimpleName(), guid, exception.getLocalizedMessage()));

        Map<String, Object> meta = Map.of("guid", guid, "path", request.getRequestURI(), "method",
                                          request.getMethod(), "timestamp", LocalDateTime.now());

        return CommonResponseDto
                .<Void>builder()
                .message(message)
                .success(false)
                .status(status.value())
                .data(null)
                .meta(meta)
                .build();
    }

    private CommonResponseDto<Void> buildValidationErrorResponse(String message, HttpStatus status,
                                                                 Map<String, String> validationErrors,
                                                                 Exception exception,
                                                                 HttpServletRequest request) {

        String guid = UUID
                .randomUUID()
                .toString();

        ColorLogger.logError(String.format("%s :: GUID=%s; message=%s", exception
                .getClass()
                .getSimpleName(), guid, exception.getLocalizedMessage()));

        Map<String, Object> meta = new HashMap<>();
        meta.put("guid", guid);
        meta.put("path", request.getRequestURI());
        meta.put("method", request.getMethod());
        meta.put("timestamp", LocalDateTime.now());
        meta.put("errors", validationErrors);

        return CommonResponseDto
                .<Void>builder()
                .message(message)
                .success(false)
                .status(status.value())
                .data(null)
                .meta(meta)
                .build();
    }
}