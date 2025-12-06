package com.nutrient_reminder.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nutrient_reminder.controller.AlarmTriggerController;
import com.nutrient_reminder.model.Nutrient;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmSchedulerService {

    private static final String ALARM_FILE = "alarms_data.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 성분 충돌 데이터베이스
    private static final Map<String, List<String>> CONFLICT_MAP = new HashMap<>();
    static {
        // 필요한 데이터 추가
    }

    public interface AlarmStatusListener {
        void onAlarmStatusChanged(String alarmId, String newStatus);
        void onDateChanged();
    }

    private static AlarmSchedulerService instance;
    private List<AlarmStatusListener> listeners = new ArrayList<>();
    private final List<Nutrient> scheduledAlarms = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private LocalDate lastCheckDate = LocalDate.now();

    private AlarmSchedulerService() {
        loadAlarmsFromFile();
        startScheduler();
    }

    public static synchronized AlarmSchedulerService getInstance() {
        if (instance == null) instance = new AlarmSchedulerService();
        return instance;
    }

    // 충돌 감지
    public String checkConflict(String newName, String newTime) {
        String conflictKey = null;
        for (String key : CONFLICT_MAP.keySet()) {
            if (newName.contains(key)) {
                conflictKey = key;
                break;
            }
        }
        if (conflictKey == null) return null;
        List<String> badCombinations = CONFLICT_MAP.get(conflictKey);

        for (Nutrient alarm : scheduledAlarms) {
            if (alarm.getTime().equals(newTime) && "ACTIVE".equals(alarm.getStatus())) {
                for (String bad : badCombinations) {
                    if (alarm.getName().contains(bad)) {
                        return String.format("주의: '%s'과(와) '%s'은(는) 함께 복용 시...", newName, alarm.getName());
                    }
                }
            }
        }
        return null;
    }

    // 스케줄러 시작
    private void startScheduler() {
        scheduler.scheduleAtFixedRate(this::checkAlarmTime, 0, 1, TimeUnit.SECONDS);
    }

    // 매 초 시간 체크
    private void checkAlarmTime() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        // 자정 체크 및 초기화 (시간 복구 포함)
        if (!today.equals(lastCheckDate)) {
            System.out.println("📅 날짜 변경 감지. 알람 초기화.");
            lastCheckDate = today;

            for (Nutrient alarm : scheduledAlarms) {
                // 스누즈 등으로 시간이 바뀌어 있다면 원래 시간으로 복구
                if (alarm.getOriginalTime() != null && !alarm.getTime().equals(alarm.getOriginalTime())) {
                    alarm.setTime(alarm.getOriginalTime());
                }
                // 완료/스누즈 상태 초기화
                if ("COMPLETED".equals(alarm.getStatus()) || "SNOOZED".equals(alarm.getStatus())) {
                    alarm.setStatus("ACTIVE");
                }
            }
            saveAlarmsToFile();
            notifyListeners("ALL", "DATE_CHANGED"); // 전체 갱신 알림
        }

        String ampm = now.getHour() < 12 ? "오전" : "오후";
        int hour = now.getHour() % 12;
        if (hour == 0) hour = 12;
        String currentTimeStr = String.format("%s %02d : %02d", ampm, hour, now.getMinute());

        String currentUserId = UserSession.getUserId();
        if (currentUserId == null) return;

        for (Nutrient alarm : scheduledAlarms) {
            if (!currentUserId.equals(alarm.getUserId())) continue;

            // 안전장치: 날짜 지났는데 완료 상태면 풀기
            if (!today.toString().equals(alarm.getLastTakenDate()) && "COMPLETED".equals(alarm.getStatus())) {
                alarm.setStatus("ACTIVE");
            }

            boolean isTodayAlarm = alarm.getDays().isEmpty() || alarm.getDays().contains(getTodayKorean());

            // ACTIVE 또는 SNOOZED 상태일 때 시간이 되면 울림
            boolean isTriggerState = "ACTIVE".equals(alarm.getStatus()) || "SNOOZED".equals(alarm.getStatus());

            if (alarm.getTime().equals(currentTimeStr) && isTriggerState && isTodayAlarm) {
                if (now.getSecond() == 0) {
                    System.out.println("🔔 알람 울림! - " + alarm.getName());
                    Platform.runLater(() -> showAlarmPopup(alarm));
                }
            }
        }
    }

    public String getTodayKorean() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        switch (day) {
            case MONDAY: return "월";
            case TUESDAY: return "화";
            case WEDNESDAY: return "수";
            case THURSDAY: return "목";
            case FRIDAY: return "금";
            case SATURDAY: return "토";
            case SUNDAY: return "일";
            default: return "";
        }
    }

    private void showAlarmPopup(Nutrient alarm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/alarmTriggerPopup.fxml"));
            Parent root = loader.load();
            AlarmTriggerController controller = loader.getController();
            controller.setAlarmInfo(alarm.getTime(), alarm.getName(), alarm.getId());
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("알람");
            stage.setScene(new Scene(root));
            stage.setAlwaysOnTop(true);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Nutrient registerAlarm(String userId, String name, String time, List<String> days, String alarmId) {
        if (alarmId == null) alarmId = "alarm_" + System.currentTimeMillis();
        Nutrient newAlarm = new Nutrient(alarmId, userId, name, time, days, "ACTIVE");
        scheduledAlarms.add(newAlarm);
        saveAlarmsToFile();
        return newAlarm;
    }

    public void updateAlarm(Nutrient updated) {
        for (int i = 0; i < scheduledAlarms.size(); i++) {
            if (scheduledAlarms.get(i).getId().equals(updated.getId())) {
                // 수정 시 원래 시간도 업데이트
                updated.setOriginalTime(updated.getTime());
                scheduledAlarms.set(i, updated);
                break;
            }
        }
        saveAlarmsToFile();
        notifyListeners(updated.getId(), "UPDATED");
    }

    public void deleteAlarm(String alarmId) {
        scheduledAlarms.removeIf(alarm -> alarm.getId().equals(alarmId));
        saveAlarmsToFile();
        notifyListeners(alarmId, "DELETED");
    }

    // 스누즈(30분 뒤) 로직 구현
    public void updateAlarmStatus(String alarmId, String status) {
        for (Nutrient alarm : scheduledAlarms) {
            if (alarm.getId().equals(alarmId)) {
                if ("COMPLETED".equals(status)) {
                    alarm.setStatus("COMPLETED");
                    alarm.setLastTakenDate(LocalDate.now().toString());
                }
                else if ("SNOOZED".equals(status)) {
                    if (alarm.getOriginalTime() == null) alarm.setOriginalTime(alarm.getTime());

                    // 30분 뒤 시간 계산
                    String newTime = add30Minutes(alarm.getTime());
                    alarm.setTime(newTime);

                    // 상태를 SNOOZED로 변경 (UI 색상 변경용)
                    alarm.setStatus("SNOOZED");
                    System.out.println("💤 30분 미룸: " + alarm.getName() + " -> " + newTime);
                }
            }
        }
        saveAlarmsToFile();
        notifyListeners(alarmId, status);
    }

    // 30분 계산 헬퍼
    private String add30Minutes(String timeStr) {
        try {
            String[] parts = timeStr.split(" ");
            String ampm = parts[0];
            int hour = Integer.parseInt(parts[1]);
            int minute = Integer.parseInt(parts[3]);
            if ("오후".equals(ampm) && hour != 12) hour += 12;
            if ("오전".equals(ampm) && hour == 12) hour = 0;
            LocalTime time = LocalTime.of(hour, minute).plusMinutes(30);
            String newAmPm = time.getHour() < 12 ? "오전" : "오후";
            int newHour = time.getHour() % 12;
            if (newHour == 0) newHour = 12;
            return String.format("%s %02d : %02d", newAmPm, newHour, time.getMinute());
        } catch (Exception e) { return timeStr; }
    }

    private void notifyListeners(String alarmId, String status) {
        Platform.runLater(() -> {
            for (AlarmStatusListener listener : listeners) {
                if ("DATE_CHANGED".equals(status)) listener.onDateChanged();
                else listener.onAlarmStatusChanged(alarmId, status);
            }
        });
    }

    private void saveAlarmsToFile() {
        try (Writer writer = new FileWriter(ALARM_FILE, StandardCharsets.UTF_8)) {
            gson.toJson(scheduledAlarms, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadAlarmsFromFile() {
        File file = new File(ALARM_FILE);
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<Nutrient>>(){}.getType();
            List<Nutrient> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                scheduledAlarms.clear();
                scheduledAlarms.addAll(loaded);
                // 데이터 호환성 (originalTime 채우기)
                for(Nutrient n : scheduledAlarms) {
                    if(n.getOriginalTime() == null) n.setOriginalTime(n.getTime());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void addListener(AlarmStatusListener listener) { listeners.add(listener); }
    public List<Nutrient> getScheduledAlarms() { return scheduledAlarms; }
}