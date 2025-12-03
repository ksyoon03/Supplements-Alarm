package com.nutrient_reminder.model;

import java.util.List;

public class Nutrient {
    private String id;
    private String userId;        // [추가] 사용자 구분용
    private String name;
    private String time;
    private List<String> days;
    private String status;
    private String lastTakenDate; // [추가] 자정 초기화 로직용 (YYYY-MM-DD)

    // Gson은 기본 생성자가 없어도 되지만, 명시적으로 두는 것이 안전합니다.
    public Nutrient() {}

    // 생성자 (userId 추가됨)
    public Nutrient(String id, String userId, String name, String time, List<String> days, String status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.time = time;
        this.days = days;
        this.status = status;
        this.lastTakenDate = "";
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLastTakenDate() { return lastTakenDate; }
    public void setLastTakenDate(String lastTakenDate) { this.lastTakenDate = lastTakenDate; }
}