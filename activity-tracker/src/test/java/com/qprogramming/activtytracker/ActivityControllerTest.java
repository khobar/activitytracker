package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.dto.Type;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import com.qprogramming.activtytracker.report.dto.ActivityReport;
import com.qprogramming.activtytracker.report.dto.Range;
import com.qprogramming.activtytracker.user.UserService;
import com.qprogramming.activtytracker.user.dto.User;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.qprogramming.activtytracker.ActivityService.DATABASE_FILE;
import static com.qprogramming.activtytracker.ActivityTestUtil.createActivities;
import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.Mockito.*;

public class ActivityControllerTest {
    private Properties propertiesMock;
    private ActivityController ctr;
    private ActivityService activityService;
    private UserService userServiceMock;

    @Before
    public void setUp() {
        propertiesMock = mock(Properties.class);
        userServiceMock = mock(UserService.class);
        URL resource = getClass().getResource("database");
        when(propertiesMock.getProperty(DATABASE_FILE)).thenReturn(resource.getFile());
        when(propertiesMock.getOrDefault(anyString(), anyLong())).then(returnsSecondArg());
        activityService = spy(new ActivityService(propertiesMock));
        ctr = new ActivityController(activityService, userServiceMock);
    }

    @Test
    public void testList() throws Exception {
        ArrayList<Activity> activities = createActivities();
        doReturn(activities).when(activityService).loadAll();
        Response response = ctr.list();
        assertEquals(200, response.getStatus());
        ArrayList<Activity> result = (ArrayList<Activity>) response.getEntity();
        assertTrue(result.size() > 1);
    }

    @Test
    public void testGetActive() throws Exception {
        ArrayList<Activity> activities = createActivities();
        doReturn(activities).when(activityService).loadAll();
        doCallRealMethod().when(activityService).getLastActive(activities);
        Activity result = (Activity) ctr.getActive().getEntity();
        assertNull(result.getEnd());
        assertEquals(1l, result.getMinutes());
    }

    @Test
    public void testGetNoActive() throws Exception {
        ArrayList<Activity> activities = createActivities();
        activities.get(1).setEnd(LocalDateTime.now().plusMinutes(1));
        doReturn(activities).when(activityService).loadAll();
        doCallRealMethod().when(activityService).getLastActive(activities);
        Activity result = (Activity) ctr.getActive().getEntity();
        assertNull(result);
    }

    @Test
    public void testStartActivity() throws IOException, ConfigurationException {
        activityService = mock(ActivityService.class);
        ctr = new ActivityController(activityService, userServiceMock);
        ArrayList<Activity> activities = createActivities();
        Activity ac = new Activity();
        doReturn(activities).when(activityService).loadAll();
        when(activityService.addNewActivity(ac, true)).then(returnsFirstArg());
        Response response = ctr.startActivity(ac);
        Activity activity = (Activity) response.getEntity();
        assertEquals(200, response.getStatus());
        assertNotNull(activity);
    }

    @Test
    public void testStopActivity() throws IOException, ConfigurationException {
        ArrayList<Activity> activities = createActivities();
        doReturn(activities).when(activityService).loadAll();
        doCallRealMethod().when(activityService).getLastActive(activities);
        Response response = ctr.stopActivity();
        Activity activity = (Activity) response.getEntity();
        verify(activityService, times(1)).saveAll(activities);
        assertEquals(200, response.getStatus());
        assertNotNull(activity);
    }

    @Test
    public void addActivity() throws IOException, ConfigurationException {
        activityService = mock(ActivityService.class);
        ctr = new ActivityController(activityService, userServiceMock);
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

    @Test
    public void testCreateActivityReport() throws IOException, ConfigurationException {
        ArrayList<Activity> activities = createActivities();
        Activity activity = new Activity();
        activity.setStart(LocalDateTime.now().minusDays(1));
        activity.setType(Type.SM);
        activity.setMinutes(80);
        activities.add(activity);
        doReturn(activities).when(activityService).loadAll();
        doCallRealMethod().when(activityService).loadDateGroupedActivities();
        doCallRealMethod().when(activityService).createActivityReport(any());
        Response dailyReport = ctr.getDailyReport(null);
        List<ActivityReport> activityReports = (List<ActivityReport>) dailyReport.getEntity();
        Optional<ActivityReport> prevActivity = activityReports.stream().filter(activityReport -> activityReport.getLocaldate().equals(LocalDate.now().minusDays(1))).findFirst();
        assertTrue(activityReports.size() > 0);
        assertTrue(prevActivity.isPresent());
        long devMinutes = prevActivity.get().getMinutes().get(Type.DEV);
        assertEquals(340L, devMinutes);
    }

    @Test
    public void testUserFound() {
        User user = new User();
        user.setApiKey("A");
        user.setSecret("B");
        user.setRole("USER");
        when(userServiceMock.getUser(user)).then(returnsFirstArg());
        Response response = ctr.getUser(user);
        User responseUser = (User) response.getEntity();
        assertEquals(user, responseUser);
    }

    @Test
    public void testUserNotFound() {
        User user = new User();
        user.setApiKey("A");
        user.setSecret("B");
        user.setRole("USER");
        Response response = ctr.getUser(user);
        assertEquals(Response.Status.FORBIDDEN, response.getStatusInfo());
        assertFalse(response.hasEntity());
    }

    @Test
    public void testDistribution() throws IOException, ConfigurationException {
        ArrayList<Activity> activities = createActivities();
        Activity activity = new Activity();
        activity.setStart(LocalDateTime.now().minusDays(1));
        activity.setType(Type.SM);
        activity.setMinutes(80);
        activities.add(activity);
        doReturn(activities).when(activityService).loadAll();
        doCallRealMethod().when(activityService).loadDateGroupedActivities();
        doCallRealMethod().when(activityService).createActivityReport(any());
        doCallRealMethod().when(activityService).fillToFullDay(any(), any());
        Response distribution = ctr.getDistribution(null);
        Map<Type, Long> distributionMap = (Map<Type, Long>) distribution.getEntity();
        assertEquals(2, distributionMap.keySet().size());
        assertEquals(71L, (long) distributionMap.get(Type.DEV));
        assertEquals(29L, (long) distributionMap.get(Type.SM));
    }

    @Test
    public void testDistributionInRange() throws IOException, ConfigurationException {
        ArrayList<Activity> activities = createActivities();
        Activity activity = new Activity();
        activity.setStart(LocalDateTime.now().minusDays(1));
        activity.setType(Type.SM);
        activity.setMinutes(80);
        activities.add(activity);
        doReturn(activities).when(activityService).loadAll();
        doCallRealMethod().when(activityService).loadDateGroupedActivities();
        doCallRealMethod().when(activityService).createActivityReport(any());
        doCallRealMethod().when(activityService).fillToFullDay(any(), any());
        Range range = new Range();
        range.setFromDate(LocalDate.now());
        Response distribution = ctr.getDistribution(range);
        Map<Type, Long> distributionMap = (Map<Type, Long>) distribution.getEntity();
        assertEquals(100L, (long) distributionMap.get(Type.SM));
    }

}
