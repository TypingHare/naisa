package com.naisa.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Carries the original search parameters and the changes permitted by the client.
 *
 * <p>Inherits field validation and the zero-budget default from {@link VenueSearchRequest}. The
 * service must keep values unchanged unless their constraints appear in {@code allowedChanges}.
 *
 * @author Andrew Hsu, Codex
 */
@JavaBean
@Getter
@Setter
public class FindAlternativesRequest extends VenueSearchRequest {
  @NotEmpty private Set<@NotNull SearchConstraint> allowedChanges;
}
