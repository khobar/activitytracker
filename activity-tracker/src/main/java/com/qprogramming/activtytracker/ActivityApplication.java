package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.filter.AuthenticationFilter;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class ActivityApplication extends ResourceConfig {
    public ActivityApplication() {
        packages("com.qprogramming.activtytracker");
        register(LoggingFilter.class);
        //Register Auth Filter here
        register(AuthenticationFilter.class);
    }
}
