package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.filter.AuthenticationFilter;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityApplication extends ResourceConfig {

    public static final String COM_QPROGRAMMING_ACTIVTYTRACKER = "com.qprogramming.activtytracker";
    private static final Logger LOGGER = Logger.getLogger(ActivityApplication.class.getName());

    public ActivityApplication() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                new FastClasspathScanner(COM_QPROGRAMMING_ACTIVTYTRACKER)
                        .matchClassesWithAnnotation(Service.class, classWithAnnotation ->
                                bind(classWithAnnotation).to(classWithAnnotation).in(Singleton.class)
                        ).scan();
                bind(loadProperties()).to(Properties.class);
            }
        });
        packages(COM_QPROGRAMMING_ACTIVTYTRACKER);
//        register(LoggingFilter.class);
        register(AuthenticationFilter.class);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        String property = System.getProperty("property.location");
        try (InputStream input = new FileInputStream(property)) {
            props.load(input);
        } catch (IOException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Fatal error while trying to read properties {}", e);
        }
        return props;
    }
}
