package com.naisa.model.vo;

import java.beans.JavaBean;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a sport offered at the venue described by a {@link VenueVo} instance.
 *
 * @author Andrew Hsu, Codex, James Chen
 */
@JavaBean
@Getter
@Setter
public class SportVo {
  private Long sportId;
  private String name;
  private BigDecimal hourlyPrice;
  private Boolean isIndoor;
  private Boolean hasLighting;
}
