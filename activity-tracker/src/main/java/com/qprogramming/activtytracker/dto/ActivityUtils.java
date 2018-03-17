package com.qprogramming.activtytracker.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class ActivityUtils {
    private static final int TYPE = 0;
    private static final int START = 1;
    private static final int END = 2;
    private static final String DELIMITER = ";";

    public static Activity fromLine(String s) {
        Activity ac = new Activity();
        if (StringUtils.isNotBlank(s)) {
            String[] bits = s.split(DELIMITER, -1);
            ac.setType(Type.valueOf(bits[TYPE]));
            ac.setStart(getDate(bits[START]));
            ac.setEnd(getDate(bits[END]));
        }
        return ac;
    }

    public static String toLine(Activity a) {
        return String.valueOf(a.getType()) +
                DELIMITER +
                a.getStart() +
                DELIMITER +
                a.getEnd();
    }


    private static Date getDate(String s) {
        if (StringUtils.isNoneBlank(s)) {
            return new Date(Long.parseLong(s));
        }
        return null;
    }
}
