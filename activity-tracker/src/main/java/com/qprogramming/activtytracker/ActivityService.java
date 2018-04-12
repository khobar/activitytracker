package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.dto.Type;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import com.qprogramming.activtytracker.report.dto.ActivityReport;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.qprogramming.activtytracker.dto.ActivityUtils.stringifyTimes;
import static com.qprogramming.activtytracker.utils.FileUtils.getFile;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;

@Service
public class ActivityService {
    public static final String DATABASE_FILE = "database.file";
    private static final String HOURS_PER_DAY = "daily.hours";
    private static long minutesPerDay;
    private Properties properties;

    @Inject
    public ActivityService(Properties props) {
        this.properties = props;
        minutesPerDay = (long) properties.getOrDefault(HOURS_PER_DAY, 7L) * 60;
    }

    /**
     * Returns last active {@link Activity}. If last activity in list have end time ( is no longer active) return null
     *
     * @param activities list of Activities
     * @return last Active task or null
     */
    public Activity getLastActive(List<Activity> activities) {
        if (activities == null || activities.size() == 0) {
            return null;
        }
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
        File dbFile = getFile(properties.getProperty(DATABASE_FILE));
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
        File dbFile = getFile(properties.getProperty(DATABASE_FILE));
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

    /**
     * Creates activity report out of entry. Each entry contains list of {@link Activity} for given {@link LocalDate}
     *
     * @param entry entry containing List of Activities for LocalDate
     * @return ActivityReport for LocalDate
     */
    public ActivityReport createActivityReport(Map.Entry<LocalDate, List<Activity>> entry) {
        ActivityReport reportEntry = new ActivityReport(entry.getKey());
        Map<Type, Long> minutes = entry.getValue().stream().collect(groupingBy(Activity::getType, summingLong(Activity::getMinutes)));
        //fill to 7h
        fillToFullDay(entry, minutes);
        reportEntry.setMinutes(minutes);
        Map<Type, Double> hours = minutes.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> ActivityUtils.getHours(e.getValue())
        ));
//        hours.replaceAll((k, v) -> v != 0 ? v / 60 : 0);
        reportEntry.setHours(hours);
        return reportEntry;
    }

    /**
     * If there were less than full day hours of activities , rest of minutes will be filled with "DEV" task
     *
     * @param entry   entry containing data for LocalDate
     * @param minutes map containing previously grouped activities and it's minutes
     */
    private void fillToFullDay(Map.Entry<LocalDate, List<Activity>> entry, Map<Type, Long> minutes) {
        if (entry.getKey().isBefore(LocalDate.now())) {//TODO to be replaced by scheduler ?
            long minutesDaySum = minutes.entrySet().stream().mapToLong(Map.Entry::getValue).sum();
            long diff = minutesPerDay - minutesDaySum;
            if (diff > 0) {
                Long debMinutes = minutes.getOrDefault(Type.DEV, 0L);
                minutes.put(Type.DEV, debMinutes + diff);
            }
        }
    }


}
