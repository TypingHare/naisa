package com.naisa.model.common;

import com.naisa.util.EarthCoordinates.WithLatitudeAndLongitude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.beans.JavaBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an earth coordinate.
 *
 * @author James Chen
 * @see <a href= "https://en.wikipedia.org/wiki/Geographic_coordinate_system">Geographic coordinate
 *      system</a>
 */
@JavaBean
@Getter
@Setter
public final class EarthCoordinate implements WithLatitudeAndLongitude {
	@NotNull
	@DecimalMin("-90")
	@DecimalMax("90")
	private Double latitude;

	@NotNull
	@DecimalMin("-180")
	@DecimalMax("180")
	private Double longitude;
}
