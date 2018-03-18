package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.util.ArrayList;

import static com.qprogramming.activtytracker.ActivityService.DATABASE_FILE;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ActivityControllerTest {
    private ActivityController ctr;
    @Mock
    private ActivityService activityService;

    @Before
    public void setUp() {
        ctr = new ActivityController(activityService);
        URL resource = getClass().getResource("database");
        System.setProperty(DATABASE_FILE, resource.getFile());
    }

    @Test
    public void testList() throws Exception {
        ArrayList<Activity> list = (ArrayList<Activity>) ctr.list().getEntity();
        assertTrue(list.size() > 1);
    }

    @Test
    public void testGetActive() throws Exception {
        ActivityController ctr = new ActivityController(activityService);
        Activity result = (Activity) ctr.getActive().getEntity();
        assertNull(result.getEnd());
    }
}