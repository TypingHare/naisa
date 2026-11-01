package com.naisa.constant;

/**
 * Defines constants related to crowd (e.g., crowd report source and crowd levels).
 *
 * @author James Chen
 */
public final class CrowdConstant {
    public static final int CROWD_REPORT_CUTOFF_MINUTES = 120;

    public static final String SOURCE_LIVE = "LIVE";
    public static final String SOURCE_UNKNOWN = "UNKNOWN";

    public static final String CROWD_LEVEL_EMPTY = "EMPTY";
    public static final String CROWD_LEVEL_LIGHT = "LIGHT";
    public static final String CROWD_LEVEL_MODERATE = "MODERATE";
    public static final String CROWD_LEVEL_BUSY = "BUSY";
    public static final String CROWD_LEVEL_PACKED = "PACKED";

    private CrowdConstant() {
    }
}
