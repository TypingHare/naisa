package com.naisa.model.vo;

import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Identifies a resource successfully saved by the service.
 *
 * @author Vincent Lai, Codex
 */
@JavaBean
@Getter
@Setter
public class ResourceIdResponse {
  private Long id;
}
