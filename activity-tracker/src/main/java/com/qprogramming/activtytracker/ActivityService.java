package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.qprogramming.activtytracker.utils.FileUtils.getFileBasedOnProperty;

@Service
public class ActivityService {
    public static final String DATABASE_FILE = "database.file";

    public Activity getLastActive(List<Activity> activities) {
        Activity lastActivity = activities.get(activities.size() - 1);
        return lastActivity.getEnd() == null ? lastActivity : null;
    }


    public List<Activity> loadAll() throws IOException, ConfigurationException {
        File dbFile = getFileBasedOnProperty(DATABASE_FILE);
        return Files.readAllLines(dbFile.toPath(), StandardCharsets.UTF_8)
                .stream()
                .map(ActivityUtils::fromLine)
                .sorted(Comparator.comparing(Activity::getStart).thenComparing(Activity::getEnd))
                .collect(Collectors.toList());
    }

    public void saveAll(List<Activity> activities) throws ConfigurationException, IOException {
        File dbFile = getFileBasedOnProperty(DATABASE_FILE);
        Files.write(dbFile.toPath(), activities
                .stream()
                .sorted(Comparator.comparing(Activity::getStart).thenComparing(Activity::getEnd))
                .map(ActivityUtils::toLine)
                .collect(Collectors.toList()));
    }
}
