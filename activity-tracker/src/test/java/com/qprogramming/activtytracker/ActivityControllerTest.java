package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.dto.Type;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.qprogramming.activtytracker.ActivityService.DATABASE_FILE;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

public class ActivityControllerTest {
    private ActivityController ctr;
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
        activity.setType(Type.SM);
        Activity activity1 = new Activity();
        activity1.setStart(LocalDateTime.now());
        activity1.setType(Type.SM);
        activities.add(activity);
        activities.add(activity1);
        return activities;
    }

    @Test
    public void testStartActivity() throws IOException, ConfigurationException {
        activityService = mock(ActivityService.class);
        ctr = new ActivityController(activityService);
        ArrayList<Activity> activities = createActivities();
        Activity ac = new Activity();
        when(activityService.loadAll()).thenReturn(activities);
        when(activityService.addNewActivity(ac, true)).then(returnsFirstArg());
        Response response = ctr.startActivity(ac);
        Activity activity = (Activity) response.getEntity();
        assertEquals(200, response.getStatus());
        assertNotNull(activity);
    }

    @Test
    public void testStopActivity() throws IOException, ConfigurationException {
        ArrayList<Activity> activities = createActivities();
        when(activityService.loadAll()).thenReturn(activities);
        when(activityService.getLastActive(activities)).thenCallRealMethod();
        Response response = ctr.stopActivity();
        Activity activity = (Activity) response.getEntity();
        verify(activityService, times(1)).saveAll(activities);
        assertEquals(200, response.getStatus());
        assertNotNull(activity);
    }

    @Test
    public void addActivity() throws IOException, ConfigurationException {
        activityService = mock(ActivityService.class);
        ctr = new ActivityController(activityService);
        ArrayList<Activity> activities = createActivities();
        Activity ac = new Activity();
        ac.setStartTime("2018-03-18T14:00");
        ac.setMinutes(61);
        ac.setType(Type.SM);
        when(activityService.loadAll()).thenReturn(activities);
        when(activityService.addNewActivity(ac, false)).then(returnsFirstArg());

        Response response = ctr.addActivity(ac);
        Activity activity = (Activity) response.getEntity();

        String expected = "SM;2018-03-18T14:00;2018-03-18T15:01;61";
        String resultString = ActivityUtils.toLine(activity);
        assertEquals(200, response.getStatus());
        assertNotNull(activity);
        assertEquals(expected, resultString);
    }

    @Test(expected = ValidationException.class)
    public void addActivityNullStart() throws IOException, ConfigurationException {
        Activity ac = new Activity();
        ac.setMinutes(61);
        ac.setType(Type.SM);
        Response response = ctr.addActivity(ac);
        fail("Validation Exception was not thrown");
    }

}
