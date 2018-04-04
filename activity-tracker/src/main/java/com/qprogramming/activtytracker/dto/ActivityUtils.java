package com.qprogramming.activtytracker.dto;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ActivityUtils {
    private static final int TYPE = 0;
    private static final int START = 1;
    private static final int END = 2;
    private static final int MINUTES = 3;

    private static final String DELIMITER = ";";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Converts string line to {@link Activity}
     *
     * @param s string line containing Activity
     * @return Activity from string
     */
    public static Activity fromLine(String s) {
        Activity ac = new Activity();
        if (StringUtils.isNotBlank(s)) {
            String[] bits = s.split(DELIMITER, -1);
            ac.setType(Type.valueOf(bits[TYPE]));
            ac.setStart(getDate(bits[START]));
            ac.setEnd(getDate(bits[END]));
            ac.setMinutes(Long.parseLong(bits[MINUTES]));
            stringifyTimes(ac);
        }
        return ac;
    }

    /**
     * Converts {@link Activity} to string line
     *
     * @param a activity to be converted
     * @return String representation of Activity
     */
    public static String toLine(Activity a) {
        if (a.getEnd() != null) {
            a.setMinutes(a.getStart().until(a.getEnd(), ChronoUnit.MINUTES));
        }
        return String.valueOf(a.getType()) +
                DELIMITER +
                dateToString(a.getStart()) +
                DELIMITER +
                dateToString(a.getEnd()) +
                DELIMITER +
                a.getMinutes();
    }

    /**
     * Updates minutes from start till end
     *
     * @param ac Activity to be updated
     */
    public static void updateMinutes(Activity ac) {
        if (ac.getEnd() != null) {
            ac.setMinutes(ac.getStart().until(ac.getEnd(), ChronoUnit.MINUTES));
        }
    }

    /**
     * Returns hours based on number of minutes ( scale 2 rounded up )
     *
     * @param minutes minutes to be converted to hours
     * @return how many hours
     */
    public static double getHours(long minutes) {
        return minutes == 0 ? 0 : BigDecimal.valueOf(minutes / 60d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static String dateToString(LocalDateTime t) {
        return t != null ? t.format(dtf) : "";
    }


    private static LocalDateTime getDate(String s) {
        if (StringUtils.isNoneBlank(s)) {
            return LocalDateTime.parse(s);
        }
        return null;
    }

    public static void stringifyTimes(Activity ac) {
        if (ac.getStart() != null) {
            ac.setStartTime(ac.getStart().format(dtf));
        }
        if (ac.getEnd() != null) {
            ac.setEndTime(ac.getEnd().format(dtf));
        }
    }
}
