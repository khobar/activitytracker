package com.qprogramming.activtytracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public class Activity {
    @JsonIgnore
    private LocalDateTime start;
    private String startTime;
    @JsonIgnore
    private LocalDateTime end;
    private String endTime;
    private long minutes;
    private Type type;

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    public double getHours() {
        return ActivityUtils.getHours(minutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Activity activity = (Activity) o;
        if (!start.equals(activity.start)) {
            return false;
        }
        if (end != null ? !end.equals(activity.end) : activity.end != null) {
            return false;
        }
        return type == activity.type;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }
}
