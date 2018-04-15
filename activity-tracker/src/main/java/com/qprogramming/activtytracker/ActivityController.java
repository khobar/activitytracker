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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qprogramming.activtytracker.dto.ActivityUtils.stringifyTimes;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;

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

    @GET
    @Path("/report")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response getDailyReport() throws ConfigurationException, IOException {
        Map<LocalDate, List<Activity>> grouped = activityService.loadDateGroupedActivities();
        List<ActivityReport> activityReports = activityService.getActivityReports(grouped);
        return Response.ok(activityReports).build();
    }


    @POST
    @Path("/distribution")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response getDistribution(Range range) throws ConfigurationException, IOException {
        Map<Type, Long> distribution = new HashMap<>();
        Arrays.stream(Type.values()).forEach(type -> distribution.put(type, 0L));
        Map<LocalDate, List<Activity>> grouped = activityService.loadDateGroupedActivities();
        if (range != null) {
            grouped = grouped.entrySet().stream().filter(isInRange(range)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        grouped.forEach((key, value) -> {
            Map<Type, Long> minutes = value.stream().collect(groupingBy(Activity::getType, summingLong(Activity::getMinutes)));
            activityService.fillToFullDay(key, minutes);
            minutes.forEach((type, aLong) -> {
                Long typeValue = distribution.get(type);
                distribution.put(type, typeValue + aLong);
            });
        });
        double total = distribution.values().stream().mapToDouble(p -> p).sum();
        Map<Type, Long> result = new HashMap<>();
        distribution.forEach((type, aLong) -> result.put(type, getPercentage(total, aLong)));
        return Response.ok(result).build();
    }

    private Long getPercentage(double total, Long aLong) {
        return aLong > 0 ? BigDecimal.valueOf((aLong / total) * 100).setScale(0, RoundingMode.HALF_UP).longValue() : 0L;
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

    /**
     * Filter entries based on pased range
     *
     * @param range Range with from-to LocalDates
     * @return predicate with result
     */
    private Predicate<Map.Entry<LocalDate, List<Activity>>> isInRange(Range range) {
        if (range.getFrom() == null) {
            return p -> !p.getKey().isAfter(range.getTo());
        } else {
            return p -> !p.getKey().isBefore(range.getFrom()) && !p.getKey().isAfter(range.getTo());
        }
    }
}
