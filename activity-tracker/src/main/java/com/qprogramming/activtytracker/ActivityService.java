package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.qprogramming.activtytracker.dto.ActivityUtils.stringifyTimes;
import static com.qprogramming.activtytracker.utils.FileUtils.getFileBasedOnProperty;

@Service
public class ActivityService {
    public static final String DATABASE_FILE = "database.file";

    /**
     * Returns last active {@link Activity}. If last activity in list have end time ( is no longer active) return null
     *
     * @param activities list of Activities
     * @return last Active task or null
     */
    public Activity getLastActive(List<Activity> activities) {
        Activity lastActivity = activities.get(activities.size() - 1);
        return lastActivity.getEnd() == null ? lastActivity : null;
    }


    /**
     * Loads all {@link Activity} from db file
     *
     * @return List of all Activities from file
     * @throws IOException            If there were errors with accesing file
     * @throws ConfigurationException If application was not run properly with parameter pointing to db file
     */
    public List<Activity> loadAll() throws IOException, ConfigurationException {
        File dbFile = getFileBasedOnProperty(DATABASE_FILE);
        return Files.readAllLines(dbFile.toPath(), StandardCharsets.UTF_8)
                .stream()
                .map(ActivityUtils::fromLine)
                .sorted(Comparator.comparing(Activity::getStart))
                .collect(Collectors.toList());
    }

    /**
     * Saves all passed list activities into file
     *
     * @param activities list of all activities to be saved
     * @throws IOException            If there were errors with accesing file
     * @throws ConfigurationException If application was not run properly with parameter pointing to db file
     */
    public void saveAll(List<Activity> activities) throws ConfigurationException, IOException {
        File dbFile = getFileBasedOnProperty(DATABASE_FILE);
        Files.write(dbFile.toPath(), activities
                .stream()
                .sorted(Comparator.comparing(Activity::getStart))
                .map(ActivityUtils::toLine)
                .collect(Collectors.toList()));
    }

    /**
     * Adds new Activity. First load all activities from file.
     * Marks last one as done if complete parameter was passed as true.
     *
     * @param ac       Activity to be added
     * @param complete if true , last active task will be marked as done ( end time set to now )
     * @return newly created {@link Activity}
     * @throws IOException            If there were errors with accesing file
     * @throws ConfigurationException If application was not run properly with parameter pointing to db file
     */
    public Activity addNewActivity(Activity ac, boolean complete) throws IOException, ConfigurationException {
        List<Activity> activities = loadAll();
        if (complete) {
            Activity lastActive = getLastActive(activities);
            if (lastActive != null) {
                lastActive.setEnd(LocalDateTime.now());
                ActivityUtils.updateMinutes(lastActive);
            }
        }
        stringifyTimes(ac);
        activities.add(ac);
        saveAll(activities);
        return ac;
    }
}
