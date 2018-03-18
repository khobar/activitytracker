package com.qprogramming.activtytracker.filter;

import com.qprogramming.activtytracker.dto.User;
import com.qprogramming.activtytracker.dto.UserUtils;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import com.qprogramming.activtytracker.exceptions.dto.ErrorMessage;
import org.glassfish.jersey.internal.util.Base64;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.qprogramming.activtytracker.utils.FileUtils.getFile;

@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {

    public static final String USERS_FILE = "users.file";
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private final Set<User> users;

    @Context
    private ResourceInfo resourceInfo;

    private Properties properties;

    @Inject
    public AuthenticationFilter(Properties props) throws IOException, ConfigurationException {
        this.properties = props;
        users = Files.readAllLines(getFile(properties.getProperty(USERS_FILE)).toPath())
                .stream()
                .map(UserUtils::fromLine)
                .collect(Collectors.toSet());
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = resourceInfo.getResourceMethod();
        if (!method.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(forbiden());
                return;
            }
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(unathorized());
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
                    requestContext.abortWith(unathorized());
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

    private Response unathorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorMessage(Response.Status.UNAUTHORIZED, "You cannot access this resource")).build();
    }
    private Response forbiden() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorMessage(Response.Status.FORBIDDEN, "Access blocked for all users !!")).build();
    }
}
