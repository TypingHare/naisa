package com.naisa.model.vo;

import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Identifies a resource successfully saved by the service.
 *
 * <p>Returning a dedicated response object avoids exposing persistence relationships and client
 * credentials through entity serialization. The surrounding response message identifies whether the
 * identifier belongs to a venue or a review.
 *
 * @author Vincent Lai, Codex
 */
@JavaBean
@Getter
@Setter
public class ResourceIdResponse {
  private Long id;

  /**
   * Creates a response containing the saved resource's identifier.
   *
   * @param id identifier assigned to the saved resource
   * @author Vincent Lai, Codex
   */
  public ResourceIdResponse(final Long id) {
    this.id = id;
  }
}