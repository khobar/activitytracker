package com.qprogramming.activtytracker.user;

import com.qprogramming.activtytracker.dto.UserUtils;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import com.qprogramming.activtytracker.user.dto.User;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.qprogramming.activtytracker.utils.FileUtils.getFile;

@Service
public class UserService {

    public static final String USERS_FILE = "users.file";
    private final Set<User> users;
    private Properties properties;

    @Inject
    public UserService(Properties props) throws IOException, ConfigurationException {
        this.properties = props;
        users = Files.readAllLines(getFile(properties.getProperty(USERS_FILE)).toPath())
                .stream()
                .map(UserUtils::fromLine)
                .collect(Collectors.toSet());
    }


    public boolean isUserAllowed(final User user, final Set<String> rolesSet) {
        boolean isAllowed = false;
        if (users.contains(user)) {
            User dbUser = getUser(user);
            if (dbUser != null) {
                isAllowed = rolesSet.contains(dbUser.getRole());
            }
        }
        return isAllowed;
    }

    public User getUser(final User user) {
        Optional<User> userOptional = users.stream().filter(db -> user.getApiKey().equals(db.getApiKey()) && user.getSecret().equals(db.getSecret())).findFirst();
        return userOptional.orElse(null);
    }

}
