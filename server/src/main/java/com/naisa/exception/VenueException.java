package com.naisa.exception;

import org.springframework.http.HttpStatus;

/**
 * Describes business-rule failures encountered when searching, creating, or reviewing venues.
 *
 * <p>
 * The static instances define reusable error descriptions. Callers must throw a fresh copy using
 * {@link #clone()} or {@link #causedBy(Throwable)} rather than throwing a shared instance.
 *
 * @author Vincent Lai, Codex, Emily Wang, Codex, Andrew Hsu, Codex
 */
public final class VenueException extends ServiceException {
	public static final VenueException REQUEST_IN_PAST = new VenueException(
		"request.validation_failed",
		"The requested date and time must not be in the past in America/New_York.",
		HttpStatus.BAD_REQUEST, null);

	public static final VenueException NO_NEARBY_VENUES = new VenueException(
		"venue.no_nearby_venues", "There are no venues for this sport nearby", HttpStatus.NOT_FOUND,
		null);

	public static final VenueException NOT_FOUND = new VenueException("venue.not_found",
		"The requested venue does not exist.", HttpStatus.NOT_FOUND, null);

	public static final VenueException INVALID_TIMEZONE = new VenueException(
		"venue.invalid_timezone",
		"Timezone must identify a recognized region such as America/New_York.",
		HttpStatus.BAD_REQUEST, null);

	public static final VenueException SPORT_NOT_OFFERED = new VenueException(
		"venue.sport_not_offered", "The requested sport is not registered at this venue.",
		HttpStatus.BAD_REQUEST, null);

	public static final VenueException DUPLICATE_IDS = new VenueException(
		"request.validation_failed", "Venue IDs must be distinct.", HttpStatus.BAD_REQUEST, null);

	public static final VenueException SPORT_NOT_FOUND = new VenueException("sport.not_found",
		"Sport does not exist.", HttpStatus.NOT_FOUND, null);

	/**
	 * Creates an error description with its HTTP status and optional underlying cause.
	 *
	 * @param code    stable error code returned to clients
	 * @param message explanation of the failed business rule
	 * @param status  HTTP status associated with the failure
	 * @param cause   underlying failure, or null when there is no underlying exception
	 * @author Vincent Lai, Codex
	 */
	private VenueException(
		final String code,
		final String message,
		final HttpStatus status,
		final Throwable cause
	) {
		super(code, message, status, cause);
	}

	/**
	 * Creates a fresh throwable with the same error description.
	 *
	 * @return a new exception with this code, message, and HTTP status
	 * @author Vincent Lai, Codex
	 */
	@Override
	public VenueException clone() {
		return new VenueException(code, getMessage(), status, null);
	}

	/**
	 * Creates a fresh throwable that retains the supplied underlying failure.
	 *
	 * @param cause underlying exception that caused this failure
	 * @return a new exception with this error description and the supplied cause
	 * @author Vincent Lai, Codex
	 */
	@Override
	public VenueException causedBy(final Throwable cause) {
		return new VenueException(code, getMessage(), status, cause);
	}
}
