package com.qprogramming.activtytracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.Map;

public class ActivityReport {

    @JsonIgnore
    private LocalDate localdate;
    private String date;
    private Map<Type, Long> minutes;
    private Map<Type, Double> hours;


    public ActivityReport(LocalDate localdate) {
        this.localdate = localdate;
        this.date = localdate.toString();
    }

    public LocalDate getLocaldate() {
        return localdate;
    }

    public String getDate() {
        return date;
    }

    public Map<Type, Long> getMinutes() {
        return minutes;
    }

    public void setMinutes(Map<Type, Long> minutes) {
        this.minutes = minutes;
    }

    public Map<Type, Double> getHours() {
        return hours;
    }

    public void setHours(Map<Type, Double> hours) {
        this.hours = hours;
    }
}
