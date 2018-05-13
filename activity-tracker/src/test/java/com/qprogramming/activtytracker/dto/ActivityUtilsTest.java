package com.qprogramming.activtytracker.dto;

import com.qprogramming.activtytracker.ActivityTestUtil;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

public class ActivityUtilsTest {

    @Test
    public void stringifyTimes() {
        ArrayList<Activity> activities = ActivityTestUtil.createActivities();
        activities.forEach(ActivityUtils::stringifyTimes);
        assertNotNull(activities.get(0).getStartTime());
    }

    @Test
    public void fromLineTest() {
        String line = "DEV;2018-03-23T08:36;2018-03-23T08:36;0";
        Activity activity = ActivityUtils.fromLine(line);
        assertNotNull(activity.getStart());
    }
}