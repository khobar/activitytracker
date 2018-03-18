package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.dto.Type;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.qprogramming.activtytracker.dto.ActivityUtils.stringifyTimes;

@Singleton
@Path("/")
public class ActivityController {

    private ActivityService activityService;

    @Context
    private Configuration configuration;

    @Inject
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String heartbeat() {
        return "{OK}";
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response list() throws ConfigurationException, IOException {
        return Response.ok(activityService.loadAll()).build();
    }

    @PUT
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response addActivity(Activity ac) throws ConfigurationException, IOException {
        if (ac.getStart() == null) {
            ac.setStart(LocalDateTime.now());
        }
        if (ac.getType() == null) {
            ac.setType(Type.SM);
        }
        List<Activity> activities = activityService.loadAll();
        Activity lastActive = activityService.getLastActive(activities);
        if (lastActive != null) {
            lastActive.setEnd(LocalDateTime.now());
            ActivityUtils.updateMinutes(lastActive);
        }
        activities.add(ac);
        activityService.saveAll(activities);
        stringifyTimes(ac);
        return Response.ok(ac).build();
    }

    @PUT
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response stopActivity() throws ConfigurationException, IOException {
        List<Activity> activities = activityService.loadAll();
        Activity lastActivity = activityService.getLastActive(activities);
        if (lastActivity != null) {
            lastActivity.setEnd(LocalDateTime.now());
            ActivityUtils.updateMinutes(lastActivity);
            activityService.saveAll(activities);
            stringifyTimes(lastActivity);
        }
        return Response.ok(lastActivity).build();
    }


    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response getActive() throws ConfigurationException, IOException {
        List<Activity> activities = activityService.loadAll();
        Activity lastActive = activityService.getLastActive(activities);
        if (lastActive != null) {
            stringifyTimes(lastActive);
        }
        return Response.ok(lastActive).build();
    }
}
