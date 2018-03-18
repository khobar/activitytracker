package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.filter.AuthenticationFilter;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;

public class ActivityApplication extends ResourceConfig {

    public static final String COM_QPROGRAMMING_ACTIVTYTRACKER = "com.qprogramming.activtytracker";

    public ActivityApplication() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                new FastClasspathScanner(COM_QPROGRAMMING_ACTIVTYTRACKER)
                        .matchClassesWithAnnotation(Service.class, classWithAnnotation ->
                                bind(classWithAnnotation).to(classWithAnnotation).in(Singleton.class)
                        ).scan();
            }
        });
        packages(COM_QPROGRAMMING_ACTIVTYTRACKER);
//        register(LoggingFilter.class);
        register(AuthenticationFilter.class);
    }
}
