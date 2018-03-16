package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Path("/activity")
public class ActivityController {

    @Context
    private Configuration configuration;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String heartbeat() {
        return "{OK}";
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        Collection<String> propertyNames = configuration.getPropertyNames();
        List<Activity> list = new ArrayList<>();
        Activity ac = new Activity();
        ac.setType(Type.DEV);
        ac.setStart(new Date());
        list.add(ac);
        return Response.ok(list).build();
    }
}
