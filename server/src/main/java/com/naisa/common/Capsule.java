package com.naisa.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.naisa.exception.ServiceException;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a capsule, which defines the structure of the JSON object in the HTTP response body.
 *
 * <p>
 * In this project, all controller methods must have a return type of
 * {@code ResponseEntity<Capsule<?>>}.
 *
 * <p>
 * This class is annotated with {@code @JsonInclude(JsonInclude.Include.NON_NULL)}, so fields with
 * null values are omitted from the serialized JSON object.
 *
 * @author James Chen
 */
@JavaBean
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Capsule<T> {
	/** A brief message to help clients understand the result of the request. */
	private String message;

	/** The data to send back to clients. */
	private T data;

	/** The code of the exception thrown while processing the request, if any. */
	private String errorCode;

	/**
	 * Creates a Capsule instance.
	 *
	 * @param message   a brief message about the result of the request
	 * @param data      the data to send back to the client
	 * @param errorCode the code of the exception thrown while processing the request
	 */
	public Capsule(final String message, final T data, final String errorCode) {
		this.message = message;
		this.data = data;
		this.errorCode = errorCode;
	}

	/**
	 * Creates a Capsule instance.
	 *
	 * @param message a brief message about the result of the request
	 * @param data    the data to send back to the client
	 */
	public Capsule(final String message, final T data) {
		this(message, data, null);
	}

	/**
	 * Converts a service exception into a capsule.
	 *
	 * <p>
	 * The returned capsule uses the service exception's {@code message} as its message and {@code
	 * code} as its error code, with data set to {@code null}.
	 *
	 * @param e the service exception to convert
	 * @return a capsule containing the service exception's message and error code
	 */
	public static Capsule<Object> fromServiceException(final ServiceException e) {
		return new Capsule<>(e.getMessage(), null, e.getCode());
	}
}
