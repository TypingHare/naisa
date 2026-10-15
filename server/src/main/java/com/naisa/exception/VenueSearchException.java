package com.naisa.exception;

import org.springframework.http.HttpStatus;

/**
 * Defines errors raised by the venue search service.
 *
 * @author Andrew Hsu
 */
public final class VenueSearchException extends ServiceException {
  public static final VenueSearchException DATA_SOURCE_NOT_IMPLEMENTED =
      new VenueSearchException(
          "search.data_source_not_implemented",
          "The venue data source is not implemented.",
          HttpStatus.NOT_IMPLEMENTED);
  public static final VenueSearchException UNKNOWN_SPORT =
      new VenueSearchException(
          "search.unknown_sport", "The sport is not stored.", HttpStatus.UNPROCESSABLE_CONTENT);
  public static final VenueSearchException UNDEFINED_GROUP_CENTER =
      new VenueSearchException(
          "request.validation_failed",
          "The group locations do not define a stable center.",
          HttpStatus.BAD_REQUEST);

  private VenueSearchException(final String code, final String message, final HttpStatus status) {
    super(code, message, status);
  }

  private VenueSearchException(
      final String code, final String message, final HttpStatus status, final Throwable cause) {
    super(code, message, status, cause);
  }

  @Override
  public VenueSearchException clone() {
    return new VenueSearchException(code, getMessage(), status);
  }

  @Override
  public VenueSearchException causedBy(final Throwable throwable) {
    return new VenueSearchException(code, getMessage(), status, throwable);
  }
}
