package com.example.costestimator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Stable error body for every non-2xx API response.
 * {@code field} is only present when the error maps to a specific request field.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String error, String message, String field) {

  public ApiError(int status, String error, String message) {
    this(status, error, message, null);
  }
}
