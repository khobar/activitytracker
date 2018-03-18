package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.qprogramming.activtytracker.ActivityService.DATABASE_FILE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivityControllerTest {
    private ActivityController ctr;
    @Mock
    private ActivityService activityService;

    @Before
    public void setUp() {
        activityService = spy(ActivityService.class);
        ctr = new ActivityController(activityService);
        URL resource = getClass().getResource("database");
        System.setProperty(DATABASE_FILE, resource.getFile());
    }

    @Test
    public void testList() throws Exception {
        ArrayList<Activity> activities = createActivities();
        when(activityService.loadAll()).thenReturn(activities);
        Response response = ctr.list();
        assertEquals(200, response.getStatus());
        ArrayList<Activity> result = (ArrayList<Activity>) response.getEntity();
        assertTrue(result.size() > 1);
    }

    @Test
    public void testGetActive() throws Exception {
        ArrayList<Activity> activities = createActivities();
        when(activityService.loadAll()).thenReturn(activities);
        when(activityService.getLastActive(activities)).thenCallRealMethod();
        Activity result = (Activity) ctr.getActive().getEntity();
        assertNull(result.getEnd());
    }

    @Test
    public void testGetNoActive() throws Exception {
        ArrayList<Activity> activities = createActivities();
        activities.get(1).setEnd(LocalDateTime.now().plusMinutes(1));
        when(activityService.loadAll()).thenReturn(activities);
        when(activityService.getLastActive(activities)).thenCallRealMethod();
        Activity result = (Activity) ctr.getActive().getEntity();
        assertNull(result);
    }


    private ArrayList<Activity> createActivities() {
        ArrayList<Activity> activities = new ArrayList<>();
        Activity activity = new Activity();
        activity.setStart(LocalDateTime.now().minusHours(1));
        activity.setEnd(LocalDateTime.now());
        Activity activity1 = new Activity();
        activity1.setStart(LocalDateTime.now());
        activities.add(activity);
        activities.add(activity1);
        return activities;
    }
}
