package com.naisa.util;

import com.naisa.model.common.EarthCoordinate;
import java.util.Collection;

/**
 * A utility class containing a collection of helper functions for {@code EarthCoordinate}.
 *
 * @author James Chen
 */
public final class EarthCoordinates {
	public static final double EARTH_RADIUS_KM = 6371.0;

	/**
	 * Creates an {@code EarthCoordinate} instance.
	 *
	 * @param latitude  the latitude of the coordinate
	 * @param longitude the longitude of the coordinate
	 * @return a created {@code EarthCoordinate} instance
	 * @author James Chen, Codex
	 */
	public static EarthCoordinate create(final double latitude, final double longitude) {
		final EarthCoordinate coordinate = new EarthCoordinate();
		coordinate.setLongitude(longitude);
		coordinate.setLatitude(latitude);
		return coordinate;
	}

	/**
	 * Creates an {@code EarthCoordinate} instance from a {@code WithLatitudeAndLongitude}
	 * implementation.
	 *
	 * @param impl an instance that implements {@code WithLatitudeAndLongitude}
	 * @author James Chen, Codex
	 */
	public static EarthCoordinate createFrom(final WithLatitudeAndLongitude impl) {
		return create(impl.getLatitude(), impl.getLongitude());
	}

	/**
	 * Calculates the center of a collection of {@code EarthCoordinate} instances by averaging their
	 * latitudes and longitudes.
	 *
	 * @param coordinates a collection of {@code EarthCoordinate} instances to average
	 * @return an {@code EarthCoordinate} representing the center of all given coordinates.
	 * @author James Chen
	 */
	public static EarthCoordinate calculateCenter(final Collection<EarthCoordinate> coordinates) {
		final int size = coordinates.size();
		final double sumOfLatitude = coordinates.stream().map(EarthCoordinate::getLatitude)
			.reduce(0.0, Double::sum);
		final double sumOfLongitude = coordinates.stream().map(EarthCoordinate::getLongitude)
			.reduce(0.0, Double::sum);

		return create(sumOfLatitude / size, sumOfLongitude / size);
	}

	/**
	 * Calculates distance between two coordinates with the Haversine formula.
	 *
	 * @param coordinate1 the first coordinate
	 * @param coordinate2 the second coordinate
	 * @return distance in kilometers
	 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
	 */
	public static double calculateDistanceKm(
		final EarthCoordinate coordinate1,
		final EarthCoordinate coordinate2
	) {
		final double latitude1 = coordinate1.getLatitude();
		final double longitude1 = coordinate1.getLongitude();
		final double latitude2 = coordinate2.getLatitude();
		final double longitude2 = coordinate2.getLongitude();
		final double latitudeDifference = Math.toRadians(latitude1 - latitude2);
		final double longitudeDifference = Math.toRadians(longitude1 - longitude2);
		final double haversine = Math.pow(Math.sin(latitudeDifference / 2), 2)
			+ Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
				* Math.pow(Math.sin(longitudeDifference / 2), 2);

		// Keep rounding errors within the square roots' valid range.
		final double boundedHaversine = Math.max(0, Math.min(1, haversine));
		final double centralAngle = 2
			* Math.atan2(Math.sqrt(boundedHaversine), Math.sqrt(1 - boundedHaversine));
		return EARTH_RADIUS_KM * centralAngle;
	}

	/** An interface providing getters for both latitude and longitude. */
	public interface WithLatitudeAndLongitude {
		Double getLatitude();

		Double getLongitude();
	}
}
