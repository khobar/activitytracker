package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Path("/activity")
public class ActivityController {

    public static final String DATABASE_FILE = "database.file";
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
    public Response list() throws ConfigurationException, IOException {
        List<Activity> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(getDataBaseFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(ActivityUtils.fromLine(line));
            }
        }
        return Response.ok(list).build();
    }

    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActive() throws ConfigurationException, IOException {
        try (ReversedLinesFileReader rlr = new ReversedLinesFileReader(getDataBaseFile(), Charset.defaultCharset())) {
            String line = rlr.readLine();
            Activity activity = ActivityUtils.fromLine(line);
            return Response.ok(activity.getEnd() == null ? activity : null).build();
        }
    }


    private File getDataBaseFile() throws ConfigurationException, IOException {
        String filename = System.getProperty(DATABASE_FILE);
        if (StringUtils.isEmpty(filename)) {
            throw new ConfigurationException("Failed to find database configuration file");
        }
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
