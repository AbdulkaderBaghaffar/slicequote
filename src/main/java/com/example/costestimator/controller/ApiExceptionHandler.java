package com.example.costestimator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.costestimator.dto.ApiError;
import com.example.costestimator.service.EmailAlreadyRegisteredException;

/**
 * Turns every application error into the stable {@link ApiError} body the
 * frontend expects, instead of Spring's default (message-less) error payload.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException e) {
    int status = e.getStatusCode().value();
    String error = reasonPhrase(status);
    String message = e.getReason() != null ? e.getReason() : error;
    return ResponseEntity.status(status).body(new ApiError(status, error, message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleValidation(MethodArgumentNotValidException e) {
    FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    if (fieldError == null) {
      return new ApiError(400, "Bad Request", "Invalid request");
    }
    return new ApiError(400, "Bad Request", fieldError.getDefaultMessage(), fieldError.getField());
  }

  @ExceptionHandler(EmailAlreadyRegisteredException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handleEmailAlreadyRegistered(EmailAlreadyRegisteredException e) {
    return new ApiError(409, "Conflict", e.getMessage(), "email");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  public ApiError handleTooLarge() {
    return new ApiError(413, "Payload Too Large", "File is too large (max 10MB)", "file");
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleMissingPart(MissingServletRequestPartException e) {
    return new ApiError(400, "Bad Request",
        "Missing required part: " + e.getRequestPartName(), e.getRequestPartName());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleMissingParameter(MissingServletRequestParameterException e) {
    return new ApiError(400, "Bad Request",
        "Missing required parameter: " + e.getParameterName(), e.getParameterName());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return new ApiError(400, "Bad Request",
        "Invalid value for '" + e.getName() + "'", e.getName());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleUnreadableBody() {
    return new ApiError(400, "Bad Request", "Malformed JSON request");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public ApiError handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
    return new ApiError(405, "Method Not Allowed", e.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handleNotFound() {
    return new ApiError(404, "Not Found", "Resource not found");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError handleDataIntegrity() {
    return new ApiError(409, "Conflict", "Request conflicts with existing data");
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleUnexpected(Exception e) {
    log.error("unhandled API error", e);
    return new ApiError(500, "Internal Server Error", "Internal server error");
  }

  private static String reasonPhrase(int status) {
    HttpStatus resolved = HttpStatus.resolve(status);
    return resolved != null ? resolved.getReasonPhrase() : "Error";
  }
}
