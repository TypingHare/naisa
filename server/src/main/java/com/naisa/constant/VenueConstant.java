package com.naisa.constant;

/**
 * Defines constants related to venues.
 *
 * @author Emily Wang, James Chen
 */
public final class VenueConstant {
    /**
     * For any incoming requests, this service does not receive a timezone. It always assumes that
     * the timezone is "America/New_York". This is intended.
     */
    public static final String REQUEST_TIMEZONE = "America/New_York";

    public static final int ALTERNATIVE_DATE_WINDOW_DAYS = 3;
    public static final int ALTERNATIVE_TIME_WINDOW_HOURS = 2;
    public static final double ALTERNATIVE_DISTANCE_INCREASE_KM = 5.0;
    public static final double ALTERNATIVE_BUDGET_INCREASE = 20.0;
    public static final int ALTERNATIVE_RESULT_LIMIT = 10;

    private VenueConstant() {
    }
}
