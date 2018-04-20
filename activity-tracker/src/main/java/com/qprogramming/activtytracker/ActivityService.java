package com.qprogramming.activtytracker;

import com.qprogramming.activtytracker.dto.Activity;
import com.qprogramming.activtytracker.dto.ActivityUtils;
import com.qprogramming.activtytracker.dto.Type;
import com.qprogramming.activtytracker.exceptions.ConfigurationException;
import com.qprogramming.activtytracker.report.dto.ActivityReport;
import com.qprogramming.activtytracker.report.dto.Range;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    public List<ActivityReport> getActivityReports(Map<LocalDate, List<Activity>> grouped) {
        return grouped.entrySet()
                .stream()
                .map(this::createActivityReport)
                .collect(Collectors.toList());
    }

    public List<Activity> loadAllInRange(Range range) throws IOException, ConfigurationException {
        return this.loadAll().stream().filter(isInRange(range)).sorted(Comparator.comparing(Activity::getStart)).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Load All activities grouped per start date in certain {@link Range}
     *
     * @param range range FROM-TO
     * @return LinkedHashMap with grouped all activities from to
     */
    public Map<LocalDate, List<Activity>> loadDateGroupedActivitiesInRange(Range range) throws IOException, ConfigurationException {
        if (range == null) {
            range = new Range();
        }
        Map<LocalDate, List<Activity>> result = new LinkedHashMap<>();
        List<Activity> activities = this.loadAllInRange(range);
        LocalDate start = range.getFrom();
        if (start == null) {
            start = activities.get(0).getStart().toLocalDate();
        }
        long days = start.until(range.getTo(), ChronoUnit.DAYS) + 1;
        List<LocalDate> allDates = Stream.iterate(start, d -> d.plusDays(1)).limit(days).filter(notWeekend()).collect(Collectors.toList());
        Map<LocalDate, List<Activity>> collect = activities.stream().collect(groupingBy(activity -> activity.getStart().toLocalDate(), Collectors.toList()));
        allDates.forEach(date -> result.put(date, getActivitiesForWorkingDays(collect, date)));
        return result;
    }

    /**
     * If for given date, there is at least one non_working activity, just discard other , and return that activity
     *
     * @param collect prepopulated all activities grouped by date
     * @param date    date for which activities should be returned
     * @return list of activities grouped for date
     */
    private List<Activity> getActivitiesForWorkingDays(Map<LocalDate, List<Activity>> collect, LocalDate date) {
        List<Activity> result = collect.getOrDefault(date, Collections.singletonList(emtpyDevActivity(date)));
        Optional<Activity> nonWorking = result.stream().filter(activity -> activity.getType() == Type.NON_WORKING).findFirst();
        return nonWorking.map(Arrays::asList).orElse(result);

    }


    /**
     * Creates empty DEV activity at certain date at 8 am
     *
     * @param date date of activity
     * @return new Activity()
     */
    private Activity emtpyDevActivity(LocalDate date) {
        Activity activity = new Activity();
        activity.setType(Type.DEV);
        activity.setStart(date.atTime(8, 0));
        activity.setEnd(date.atTime(8, 0));
        activity.setMinutes(0);
        return activity;
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
        fillToFullDay(entry.getKey(), minutes);
        reportEntry.setMinutes(minutes);
        Map<Type, Double> hours = minutes.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> ActivityUtils.getHours(e.getValue())
        ));
        reportEntry.setHours(hours);
        return reportEntry;
    }

    /**
     * Returns distribution of task in given range
     *
     * @param range Range from - to
     * @return Map of Type - Minutes total in range
     */
    public Map<Type, Long> getDistributionInRange(Range range) throws IOException, ConfigurationException {
        Map<Type, Long> distribution = new HashMap<>();
        Arrays.stream(Type.values()).forEach(type -> distribution.put(type, 0L));
        Map<LocalDate, List<Activity>> grouped = this.loadDateGroupedActivitiesInRange(range);
        grouped.forEach((key, value) -> {
            Map<Type, Long> minutes = value.stream().collect(groupingBy(Activity::getType, summingLong(Activity::getMinutes)));
            this.fillToFullDay(key, minutes);
            minutes.forEach((type, aLong) -> {
                Long typeValue = distribution.get(type);
                distribution.put(type, typeValue + aLong);
            });
        });
        double total = distribution.values().stream().mapToDouble(p -> p).sum();
        Map<Type, Long> result = new HashMap<>();
        distribution.forEach((type, aLong) -> result.put(type, getPercentage(total, aLong)));
        return result;
    }


    /**
     * If there were less than full day hours of activities , rest of minutes will be filled with "DEV" task
     *
     * @param date    data for LocalDate
     * @param minutes map containing previously grouped activities and it's minutes
     */
    public void fillToFullDay(LocalDate date, Map<Type, Long> minutes) {
        if (date.isBefore(LocalDate.now())) {//TODO to be replaced by scheduler ?
            long minutesDaySum = minutes.entrySet().stream().mapToLong(Map.Entry::getValue).sum();
            long diff = minutesPerDay - minutesDaySum;
            if (diff > 0) {
                Long debMinutes = minutes.getOrDefault(Type.DEV, 0L);
                minutes.put(Type.DEV, debMinutes + diff);
            }
        }
    }
    public Activity addNonWorkingActivity(LocalDate localDate) throws IOException, ConfigurationException {
        Activity activity = new Activity(Type.NON_WORKING);
        activity.setStart(localDate.atTime(8, 0));
        activity.setEnd(activity.getStart().plusMinutes(minutesPerDay));
        return addNewActivity(activity,false);
    }

    /**
     * Filter entries based on pased range
     *
     * @param range Range with from-to LocalDates
     * @return predicate with result
     */
    private Predicate<Activity> isInRange(Range range) {
        if (range.getFrom() == null) {
            return p -> !p.getStart().toLocalDate().isAfter(range.getTo());
        } else {
            return p -> !p.getStart().toLocalDate().isBefore(range.getFrom()) && !p.getStart().toLocalDate().isAfter(range.getTo());
        }
    }

    private Predicate<LocalDate> notWeekend() {
        return d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private Long getPercentage(double total, Long aLong) {
        return aLong > 0 ? BigDecimal.valueOf((aLong / total) * 100).setScale(0, RoundingMode.HALF_UP).longValue() : 0L;
    }

}
