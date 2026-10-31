package com.naisa.constant;

/**
 * Defines constants related to venues.
 *
 * @author Emily Wang, James Chen
 */
public final class VenueConstant {
    /** Number of hours a crowd report is considered recent. */
    public static final int CROWD_REPORT_CUTOFF_HOURS = 2;

    public static final String SEARCH_TIMEZONE = "America/New_York";
    public static final int ALTERNATIVE_DATE_WINDOW_DAYS = 3;
    public static final int ALTERNATIVE_TIME_WINDOW_HOURS = 2;
    public static final double ALTERNATIVE_DISTANCE_INCREASE_KM = 5.0;
    public static final double ALTERNATIVE_BUDGET_INCREASE = 20.0;
    public static final int ALTERNATIVE_RESULT_LIMIT = 10;

    private VenueConstant() {
    }
}
