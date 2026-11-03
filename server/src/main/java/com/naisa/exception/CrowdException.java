package com.naisa.exception;

import org.springframework.http.HttpStatus;

/**
 * Describes business-rule failures when submitting crowd reports or retrieving crowd status.
 *
 * @author James Chen
 */
public final class CrowdException extends ServiceException {
    public static final CrowdException INVALID_CROWD_LEVEL_SCORE = new CrowdException(
        "crowd.invalid_crowd_level_score",
        "A score less than 0.0 or greater than 4.0 is invalid.",
        HttpStatus.INTERNAL_SERVER_ERROR,
        null
    );

    public static final CrowdException INVALID_CROWD_LEVEL_STRING = new CrowdException(
        "crowd.invalid_crowd_level_string",
        "A crowd level string must be one of: EMPTY, LIGHT, MODERATE, BUSY, PACKED",
        HttpStatus.INTERNAL_SERVER_ERROR,
        null
    );

    /**
     * Creates a crowd-related exception with an error code, HTTP status, and optional cause.
     *
     * @param code    stable error code returned to clients
     * @param message explanation of the failed business rule
     * @param status  HTTP status associated with the failure
     * @param cause   underlying failure, or null when there is no underlying exception
     * @author James Chen
     */
    private CrowdException(
        final String code,
        final String message,
        final HttpStatus status,
        final Throwable cause
    ) {
        super(code, message, status, cause);
    }

    /**
     * Creates a new exception with the same error description and no underlying cause.
     *
     * @return a new exception with this code, message, and HTTP status, without a cause
     * @author James Chen
     */
    @Override
    public CrowdException clone() {
        return new CrowdException(code, getMessage(), status, null);
    }

    /**
     * Creates a new exception with the same error description and the supplied cause.
     *
     * @param cause underlying exception that caused this failure
     * @return a new exception with this error description and the supplied cause
     * @author James Chen
     */
    @Override
    public CrowdException causedBy(final Throwable cause) {
        return new CrowdException(code, getMessage(), status, cause);
    }
}
