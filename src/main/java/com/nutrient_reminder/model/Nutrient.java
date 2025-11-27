package com.nutrient_reminder.model;

import java.util.List;

public class Nutrient {
    private String name;
    private String time;
    private List<String> days;

    public Nutrient(String name, String time, List<String> days) {
        this.name = name;
        this.time = time;
        this.days = days;
    }

    public String getName() { return name; }
    public String getTime() { return time; }
    public List<String> getDays() { return days; }
}