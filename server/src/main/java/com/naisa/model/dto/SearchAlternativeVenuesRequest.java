package com.naisa.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Parameters for searching for alternative venues.
 *
 * <p>
 * Extends {@link SearchVenuesRequest} with the search constraints the client permits changing.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class SearchAlternativeVenuesRequest extends SearchVenuesRequest {
	@NotEmpty
	private Set<@NotNull SearchConstraint> allowedChanges;

	/**
	 * Identifies a search value that the client permits the service to change.
	 *
	 * @author Andrew Hsu, Codex, James Chen
	 */
	public enum SearchConstraint {
		DATE, TIME, MAX_DISTANCE, BUDGET
	}
}
