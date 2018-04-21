package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.dto.Type;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import com.qprogramming.activtytracker.report.dto.ActivityReport;
import com.qprogramming.activtytracker.report.dto.Range;
import com.qprogramming.activtytracker.user.UserService;
import com.qprogramming.activtytracker.user.dto.User;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static com.qprogramming.activtytracker.dto.ActivityUtils.stringifyTimes;

@Singleton
@Path("/")
public class ActivityController {

    private ActivityService activityService;
    private UserService userService;

    @Context
    private Configuration configuration;

    @Inject
    public ActivityController(ActivityService activityService, UserService userService) {
        this.activityService = activityService;
        this.userService = userService;
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

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response addActivity(Activity ac) throws ConfigurationException, IOException {
        if (StringUtils.isEmpty(ac.getStartTime())) {
            throw new ValidationException("No start date time in passed activity");
        }
        ac.setStart(LocalDateTime.parse(ac.getStartTime()));
        if (ac.getEndTime() == null && ac.getMinutes() != 0) {
            ac.setEnd(ac.getStart().plusMinutes(ac.getMinutes()));
        } else {
            ac.setEnd(LocalDateTime.parse(ac.getEndTime()));
        }
        ac = activityService.addNewActivity(ac, false);
        return Response.ok(ac).build();
    }


    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response startActivity(Activity ac) throws ConfigurationException, IOException {
        if (ac.getStart() == null) {
            ac.setStart(LocalDateTime.now());
        }
        if (ac.getType() == null) {
            ac.setType(Type.SM);
        }
        ac = activityService.addNewActivity(ac, true);
        return Response.ok(ac).build();
    }

    @POST
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
            lastActive.setMinutes(lastActive.getStart().until(LocalDateTime.now(), ChronoUnit.MINUTES));
        }
        return Response.ok(lastActive).build();
    }

    @POST
    @Path("/report")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response getDailyReport(Range range) throws ConfigurationException, IOException {
        Map<LocalDate, List<Activity>> grouped = activityService.loadDateGroupedActivitiesInRange(range);
        List<ActivityReport> activityReports = activityService.getActivityReports(grouped);
        return Response.ok(activityReports).build();
    }


    @POST
    @Path("/distribution")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response getDistribution(Range range) throws ConfigurationException, IOException {
        Map<Type, Long> result = activityService.getDistributionInRange(range);
        return Response.ok(result).build();
    }


    @POST
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getUser(User user) {
        User dbUser = userService.getUser(user);
        if (dbUser == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(dbUser).build();
    }

    @POST
    @Path("/add-non-working")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response addNotWorkingDay(String date) throws ConfigurationException, IOException {
        if (StringUtils.isEmpty(date)) {
            throw new ValidationException("No date passed");
        }
        Activity activity = activityService.addNonWorkingActivity(LocalDate.parse(date));
        return Response.ok(activity).build();
    }


}
