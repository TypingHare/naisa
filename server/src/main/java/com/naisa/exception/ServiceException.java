package com.naisa.exception;

import com.naisa.common.Capsule;
import org.springframework.http.HttpStatus;

/**
 * Represents a service exception.
 *
 * <p>
 * A service exception should only be thrown in the service layer (i.e., classes in the {@code
 * com.naisa.service} package).
 *
 * <p>
 * A service exception can be converted into a capsule to form an error response. Its code and
 * message are used in {@code Capsule}, and its status is used as the HTTP response status.
 *
 * @see Capsule
 * @author James Chen
 */
public class ServiceException extends RuntimeException implements Cloneable {
	protected final String code;
	protected final HttpStatus status;

	protected ServiceException(
		final String code,
		final String message,
		final HttpStatus status,
		final Throwable cause
	) {
		super(message, cause);

		this.code = code;
		this.status = status;
	}

	protected ServiceException(final String code, final String message, final HttpStatus status) {
		this(code, message, status, null);
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public ServiceException clone() {
		return new ServiceException(code, this.getMessage(), status);
	}

	public ServiceException causedBy(final Throwable throwable) {
		return new ServiceException(code, this.getMessage(), status, throwable);
	}
}
