package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.qprogramming.activtytracker.ActivityService.DATABASE_FILE;
import static org.junit.Assert.*;

public class ActivityServiceTest {

    private ActivityService activityService;

    @Before
    public void setUp() {
        activityService = new ActivityService();
        URL resource = getClass().getResource("database");
        System.setProperty(DATABASE_FILE, resource.getFile());
    }

    @Test
    public void testLoadAll() throws IOException, ConfigurationException {
        List<Activity> activities = activityService.loadAll();
        assertTrue(activities.size() > 1);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadAllException() throws IOException, ConfigurationException {
        System.clearProperty(DATABASE_FILE);
        List<Activity> activities = activityService.loadAll();
        fail("Exception was not thrown");
    }

    @Test
    public void testLoadLastActive() throws IOException, ConfigurationException {
        List<Activity> activities = activityService.loadAll();
        Activity lastActive = activityService.getLastActive(activities);
        assertNotNull(lastActive);
    }

    @Test
    public void testLoadLastActiveNotFound() {
        Activity ac1 = new Activity();
        ac1.setStart(LocalDateTime.now());
        ac1.setEnd(LocalDateTime.now().plusHours(1));
        Activity ac2 = new Activity();
        ac2.setStart(LocalDateTime.now().plusHours(1));
        ac2.setEnd(LocalDateTime.now().plusHours(2));
        List<Activity> activities = Arrays.asList(ac1, ac2);
        Activity lastActive = activityService.getLastActive(activities);
        assertNull(lastActive);
    }

}

