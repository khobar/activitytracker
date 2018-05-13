package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.Type;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ActivityTestUtil {

    public static ArrayList<Activity> createActivities() {
        ArrayList<Activity> activities = new ArrayList<>();
        Activity activity = new Activity();
        activity.setStart(ZonedDateTime.now().minusHours(1));
        activity.setEnd(ZonedDateTime.now());
        activity.setType(Type.SM);
        activity.setMinutes(60);
        Activity activity1 = new Activity();
        activity1.setStart(ZonedDateTime.now().minusMinutes(1));
        activity1.setType(Type.SM);
        activities.add(activity);
        activities.add(activity1);
        return activities;
    }
}
