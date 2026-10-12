package com.naisa.exception;

import org.springframework.http.HttpStatus;

/**
 * Represents a client exception.
 *
 * @author James Chen
 */
public final class ClientException extends ServiceException {
  public static final ClientException INVALID_API_KEY =
      new ClientException("client.invalid_api_key", "Invalid API key.", HttpStatus.UNAUTHORIZED);

  protected ClientException(final String code, final String message, final HttpStatus status) {
    super(code, message, status);
  }

  protected ClientException(
      final String code, final String message, final HttpStatus status, final Throwable throwable) {
    super(code, message, status, throwable);
  }

  @Override
  public ClientException clone() {
    return new ClientException(code, this.getMessage(), status);
  }

  @Override
  public ClientException causedBy(final Throwable throwable) {
    return new ClientException(code, this.getMessage(), status, throwable);
  }
}
