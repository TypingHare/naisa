package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Describes a sport offered at a venue.
 *
 * <p>Null lighting availability means unknown.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class VenueSportResponse {
  private Long sportId;
  private String name;
  private BigDecimal hourlyPrice;
  private Boolean isIndoor;
  private Boolean hasLighting;
}
