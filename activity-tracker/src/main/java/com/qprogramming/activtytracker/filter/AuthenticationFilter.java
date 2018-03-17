package com.qprogramming.activtytracker.filter;

import com.qprogramming.activtytracker.dto.User;
import com.qprogramming.activtytracker.dto.UserUtils;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.glassfish.jersey.internal.util.Base64;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static com.qprogramming.activtytracker.utils.FileUtils.getFileBasedOnProperty;

@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {

    public static final String USERS_FILE = "users.file";
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED)
            .entity("{You cannot access this resource}").build();
    private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
            .entity("{Access blocked for all users !!}").build();
    private final Set<User> users;

    @Context
    private ResourceInfo resourceInfo;

    public AuthenticationFilter() throws IOException, ConfigurationException {
        users = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(getFileBasedOnProperty(USERS_FILE)))) {
            String line;
            while ((line = br.readLine()) != null) {
                users.add(UserUtils.fromLine(line));
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = resourceInfo.getResourceMethod();
        if (!method.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }
            final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
            String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
            final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
            final String apiKey = tokenizer.nextToken();
            final String secret = tokenizer.nextToken();
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
                User user = new User(apiKey, secret);
                if (!isUserAllowed(user, rolesSet)) {
                    requestContext.abortWith(ACCESS_DENIED);
                }
            }
        }
    }

    private boolean isUserAllowed(final User user, final Set<String> rolesSet) {
        boolean isAllowed = false;
        if (users.contains(user)) {
            Optional<User> userOptional = users.stream().filter(db -> user.getApiKey().equals(db.getApiKey()) && user.getSecret().equals(db.getSecret())).findFirst();
            if (userOptional.isPresent()) {
                isAllowed = rolesSet.contains(userOptional.get().getRole());
            }
        }
        return isAllowed;
    }
}
