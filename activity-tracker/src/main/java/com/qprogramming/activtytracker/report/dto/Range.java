package com.qprogramming.activtytracker.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Range {
    private static final String ISO_LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_LOCAL_DATE_PATTERN)
    private LocalDate from;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_LOCAL_DATE_PATTERN)
    private LocalDate to;

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(String from) {
        setFromDate(LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public void setFromDate(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        if (to == null) {
            to = LocalDate.now();
        }
        return to;
    }

    public void setTo(String to) {
        setToDate(LocalDate.parse(to, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public void setToDate(LocalDate to) {
        this.to = to;
    }
}
