package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;

import static com.qprogramming.activtytracker.ActivityController.DATABASE_FILE;

public class ActivityControllerTest extends TestCase {
    private ActivityController ctr;

    @Before
    public void setUp() {
        ctr = new ActivityController();
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
        ActivityController ctr = new ActivityController();
        Activity result = (Activity) ctr.getActive().getEntity();
        assertTrue(result.getEnd() == null);
    }
}