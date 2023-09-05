package com.xm.cryptorecservice.util;

import java.time.format.DateTimeFormatter;

/**
 * Various global constants used by our app.
 *
 * @author jason
 */
public final class Constants {

    private Constants() {}

    /**
     * Standard authentication header prefix.
     */
    public static final String AUTH_HEADER_BEARER_PREFIX = "Bearer" + " ";
    /** Tune this to affect how long the JWT token lasts. Default is 5 * 60 * 60, for 5 hours. */
    public static final long JWT_VALIDITY = 5 * 60 * 60;

    /**
     * Our global date pattern. Conforms to the American standard of
     * month before days, only because our version of MySQL also did the same and it made things easy.
     */
    public static final String GLOBAL_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * A {@link DateTimeFormatter} built from {@link #GLOBAL_DATE_PATTERN}.
     */
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(GLOBAL_DATE_PATTERN);

    /**
     * Our global date-time pattern, with accuracy up to milliseconds.
     */
    public static final String GLOBAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * A {@link DateTimeFormatter} built from {@link #GLOBAL_DATE_TIME_PATTERN}.
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(GLOBAL_DATE_TIME_PATTERN);

    /**
     *  Maximum number of decimal digits we allow in a {@link java.math.BigDecimal} instance in our app.
     */
    public static final int BIG_DECIMAL_SCALE = 10;

    /**
     * Maximum number of threads that we employ in fixed thread pools
     * to parallelize tasks across several cryptos.
     */
    public static final int MAX_THREADS = 10;

}
