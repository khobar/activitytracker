package com.qprogramming.activtytracker.dto;

import com.qprogramming.activtytracker.user.dto.User;
import org.apache.commons.lang3.StringUtils;

public class UserUtils {
    private static final int API = 0;
    private static final int SECRET = 1;
    private static final int ROLE = 2;
    private static final String DELIMITER = ";";

    public static User fromLine(String s) {
        User us = new User();
        if (StringUtils.isNotBlank(s)) {
            String[] bits = s.split(DELIMITER, -1);
            us.setApiKey(bits[API]);
            us.setSecret(bits[SECRET]);
            us.setRole(bits[ROLE]);
        }
        return us;
    }

}
