package com.nutrient_reminder.model;

import java.util.List;

public class Nutrient {
    private String id;
    private String name;
    private String time;
    private List<String> days;
    private String status;

    // ID 및 상태를 포함한 생성자로 수정 ++
    public Nutrient(String id, String name, String time, List<String> days, String status) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.days = days;
        this.status = status;
    }

    public String getId() { return id; }    // ID Getter 추가
    public String getName() { return name; }
    public String getTime() { return time; }
    public List<String> getDays() { return days; }
    public String getStatus() { return status; } // Status Getter 추가

    // Status Setter 추가 (필요 시 상태 업데이트)
    public void setStatus(String status) {
        this.status = status;
    }
}