package com.naisa.exception;

import org.springframework.http.HttpStatus;

/**
 * Defines request and lookup errors for venue info.
 *
 * @author Andrew Hsu
 */
public final class VenueInfoException extends ServiceException {
  public static final VenueInfoException INVALID_SELECTOR =
      new VenueInfoException(
          "request.validation_failed",
          "Give one valid venueId or venueName.",
          HttpStatus.BAD_REQUEST);
  public static final VenueInfoException NOT_FOUND =
      new VenueInfoException("venue.not_found", "The venue was not found.", HttpStatus.NOT_FOUND);
  public static final VenueInfoException AMBIGUOUS_NAME =
      new VenueInfoException(
          "venue.ambiguous_name",
          "More than one venue has this name. Use venueId.",
          HttpStatus.CONFLICT);

  private VenueInfoException(final String code, final String message, final HttpStatus status) {
    super(code, message, status);
  }

  private VenueInfoException(
      final String code, final String message, final HttpStatus status, final Throwable cause) {
    super(code, message, status, cause);
  }

  @Override
  public VenueInfoException clone() {
    return new VenueInfoException(code, getMessage(), status);
  }

  @Override
  public VenueInfoException causedBy(final Throwable throwable) {
    return new VenueInfoException(code, getMessage(), status, throwable);
  }
}
