package com.naisa.controller;

import com.naisa.common.Capsule;
import com.naisa.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This global exception handler handles exceptions caught during request handling.
 *
 * <p>For service exceptions, it sets the HTTP status to the value of the exception's status field
 * and uses the object returned by {@code Capsule.fromServiceException()} as the response body.
 *
 * <p>For exceptions other than service exceptions and those handled specifically by this class, it
 * sets the HTTP status to 500 Internal Server Error and the error code to {@code
 * ERROR_CODE_SERVER_INTERNAL_ERROR}.
 *
 * @author James Chen
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  public static final String ERROR_CODE_SERVER_INTERNAL_ERROR = "server.internal_error";
  public static final String ERROR_CODE_REQUEST_INVALID_BODY = "request.invalid_body";
  public static final String ERROR_CODE_REQUEST_VALIDATION_FAILED = "request.validation_failed";

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Capsule<Object>> handleUnexpectedException(final Exception exception) {
    LOGGER.error("Unexpected error while handling request", exception);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new Capsule<>(
                "An internal server error occurred.", null, ERROR_CODE_SERVER_INTERNAL_ERROR));
  }

  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<Capsule<Object>> handleServiceException(final ServiceException exception) {
    return ResponseEntity.status(exception.getStatus())
        .body(Capsule.fromServiceException(exception));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Capsule<Object>> handleUnreadableBody(
      final HttpMessageNotReadableException exception) {
    return ResponseEntity.badRequest()
        .body(
            new Capsule<>(
                "Request body is missing or invalid.", null, ERROR_CODE_REQUEST_INVALID_BODY));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Capsule<Object>> handleValidationFailure(
      final MethodArgumentNotValidException exception) {
    return ResponseEntity.badRequest()
        .body(
            new Capsule<>(
                "Request validation failed.", null, ERROR_CODE_REQUEST_VALIDATION_FAILED));
  }
}
