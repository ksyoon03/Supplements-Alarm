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
import java.time.Duration; // 시간 차이 계산용
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

    // 1. 성분 충돌 데이터베이스
    private static final Map<String, List<String>> CONFLICT_MAP = new HashMap<>();
    static {
        // [미네랄]
        CONFLICT_MAP.put("철분", List.of("칼슘", "마그네슘", "아연", "녹차", "커피", "종합비타민"));
        CONFLICT_MAP.put("칼슘", List.of("철분", "인", "아연")); // 아연 흡수도 방해
        CONFLICT_MAP.put("아연", List.of("철분", "칼슘"));
        CONFLICT_MAP.put("마그네슘", List.of("철분"));

        // [지용성 비타민 & 루테인]
        CONFLICT_MAP.put("비타민A", List.of("루테인", "지아잔틴"));
        CONFLICT_MAP.put("비타민 A", List.of("루테인", "지아잔틴"));
        CONFLICT_MAP.put("루테인", List.of("비타민A", "비타민 A"));

        // [오일류 & 흡착]
        CONFLICT_MAP.put("오메가3", List.of("키토산"));
        CONFLICT_MAP.put("오메가-3", List.of("키토산"));
        CONFLICT_MAP.put("키토산", List.of("오메가3", "오메가-3", "비타민A", "비타민D", "비타민E"));

        // [약물 상호작용]
        CONFLICT_MAP.put("유산균", List.of("항생제", "프로폴리스"));
        CONFLICT_MAP.put("프로바이오틱스", List.of("항생제", "프로폴리스"));

        CONFLICT_MAP.put("테아닌", List.of("카페인", "커피", "녹차"));

        CONFLICT_MAP.put("가르시니아", List.of("당뇨", "인슐린", "메트포르민"));
        CONFLICT_MAP.put("홍삼", List.of("당뇨", "혈압", "아스피린", "와파린"));

        // [흡수 방해]
        CONFLICT_MAP.put("식이섬유", List.of("칼슘", "철분", "아연", "마그네슘", "미네랄"));
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

    // [수정됨] 충돌 감지 로직 (시간 범위 ±2분 적용)
    public String checkConflict(String newName, String newTime) {

        for (Nutrient alarm : scheduledAlarms) {
            // 1. 활성 상태인지 확인
            boolean isActive = "ACTIVE".equals(alarm.getStatus()) || "SNOOZED".equals(alarm.getStatus());
            if (!isActive) continue;

            // 2. 시간 차이 검사 (±2분 이내면 충돌로 간주)
            if (isTimeConflict(newTime, alarm.getTime())) {
                String existingName = alarm.getName();

                // 3. 성분 충돌 메시지 생성
                String message = getConflictMessage(newName, existingName);
                if (message != null) {
                    return message;
                }
            }
        }
        return null;
    }

    // 시간 비교 헬퍼 메서드 (±2분 체크)
    private boolean isTimeConflict(String time1Str, String time2Str) {
        try {
            LocalTime t1 = parseTime(time1Str);
            LocalTime t2 = parseTime(time2Str);

            // 두 시간의 차이(분) 절대값 계산
            long diff = Math.abs(Duration.between(t1, t2).toMinutes());

            // 차이가 2분 이내면 true (0, 1, 2분 차이)
            return diff <= 2;
        } catch (Exception e) {
            return false; // 파싱 에러 시 충돌 아님 처리
        }
    }

    // 문자열 시간 -> LocalTime 변환기
    private LocalTime parseTime(String timeStr) {
        // "오전 09 : 30" -> 분해
        String[] parts = timeStr.split("[:\\s]+"); // 공백이나 콜론으로 분리
        String ampm = parts[0];
        int hour = Integer.parseInt(parts[1]);
        int minute = Integer.parseInt(parts[2]);

        if ("오후".equals(ampm) && hour != 12) hour += 12;
        if ("오전".equals(ampm) && hour == 12) hour = 0;

        return LocalTime.of(hour, minute);
    }

    // 상세 경고 메시지 생성기
    private String getConflictMessage(String name1, String name2) {
        // 비타민A/a, 비타민C/c 둘 다 가능
        String n1 = name1.toUpperCase();
        String n2 = name2.toUpperCase();

        // 1. 칼슘 <-> 아연
        if (hasPair(n1, n2, "칼슘", "아연")) {
            return "칼슘 섭취량이 많으면 아연 흡수가 저하될 수 있습니다.";
        }
        // 2. 철분 <-> 칼슘
        if (hasPair(n1, n2, "철분", "칼슘")) {
            return "철분과 칼슘은 서로 흡수를 강력하게 방해합니다.\n" +
                    "동시 섭취를 피하고 아침/저녁으로 나눠 드시는 것을 권장합니다.";
        }
        // 3. 철분 <-> 아연
        if (hasPair(n1, n2, "철분", "아연")) {
            return "철분과 아연은 흡수 경로가 같아 서로 경쟁합니다.";
        }
        // 4. 철분 <-> 마그네슘
        if (hasPair(n1, n2, "철분", "마그네슘")) {
            return "철분은 마그네슘 흡수를 방해할 수 있습니다.\n" +
                    "철분은 공복일 때, 마그네슘은 저녁 식후에 드시는 것을 권장합니다.";
        }

        // 5. 비타민A <-> 루테인
        if (hasPair(n1, n2, "비타민A", "루테인")) {
            if (n1.contains("A") || n2.contains("A")) {
                return "고함량 비타민A와 루테인은 성질이 비슷해 동시 섭취 시 흡수 효율이 떨어질 수 있습니다.";
            }
        }

        // 6. 유산균 <-> 비타민C
        if (hasPair(n1, n2, "유산균", "비타민C")) {
            if (n1.contains("C") || n2.contains("C")) {
                return "유산균은 산성에 약해 비타민C와 유산균을 함께 드신다면 유산균이 장에 도달하기 전에 죽을 수도 있습니다.";
            }
        }

        return null;
    }

    private boolean hasPair(String name1, String name2, String k1, String k2) {
        return (name1.contains(k1) && name2.contains(k2)) || (name1.contains(k2) && name2.contains(k1));
    }

    // --- 스케줄러 및 기타 로직  ---
    private void startScheduler() {
        scheduler.scheduleAtFixedRate(this::checkAlarmTime, 0, 1, TimeUnit.SECONDS);
    }

    private void checkAlarmTime() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        if (!today.equals(lastCheckDate)) {
            lastCheckDate = today;
            for (Nutrient alarm : scheduledAlarms) {
                if (alarm.getOriginalTime() != null && !alarm.getTime().equals(alarm.getOriginalTime())) {
                    alarm.setTime(alarm.getOriginalTime());
                }
                if ("COMPLETED".equals(alarm.getStatus()) || "SNOOZED".equals(alarm.getStatus())) {
                    alarm.setStatus("ACTIVE");
                }
            }
            saveAlarmsToFile();
            notifyListeners("ALL", "DATE_CHANGED");
        }

        String ampm = now.getHour() < 12 ? "오전" : "오후";
        int hour = now.getHour() % 12;
        if (hour == 0) hour = 12;
        String currentTimeStr = String.format("%s %02d : %02d", ampm, hour, now.getMinute());

        String currentUserId = UserSession.getUserId();
        if (currentUserId == null) return;

        for (Nutrient alarm : scheduledAlarms) {
            if (!currentUserId.equals(alarm.getUserId())) continue;
            if (!today.toString().equals(alarm.getLastTakenDate()) && "COMPLETED".equals(alarm.getStatus())) {
                alarm.setStatus("ACTIVE");
            }
            boolean isTodayAlarm = alarm.getDays().isEmpty() || alarm.getDays().contains(getTodayKorean());
            boolean isTriggerState = "ACTIVE".equals(alarm.getStatus()) || "SNOOZED".equals(alarm.getStatus());

            if (alarm.getTime().equals(currentTimeStr) && isTriggerState && isTodayAlarm) {
                if (now.getSecond() == 0) {
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

    public void updateAlarmStatus(String alarmId, String status) {
        for (Nutrient alarm : scheduledAlarms) {
            if (alarm.getId().equals(alarmId)) {
                if ("COMPLETED".equals(status)) {
                    alarm.setStatus("COMPLETED");
                    alarm.setLastTakenDate(LocalDate.now().toString());
                }
                else if ("SNOOZED".equals(status)) {
                    if (alarm.getOriginalTime() == null) alarm.setOriginalTime(alarm.getTime());
                    String newTime = add30Minutes(alarm.getTime());
                    alarm.setTime(newTime);
                    alarm.setStatus("SNOOZED");
                }
            }
        }
        saveAlarmsToFile();
        notifyListeners(alarmId, status);
    }

    private String add30Minutes(String timeStr) {
        try {
            LocalTime time = parseTime(timeStr).plusMinutes(30);
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
                for(Nutrient n : scheduledAlarms) {
                    if(n.getOriginalTime() == null) n.setOriginalTime(n.getTime());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void addListener(AlarmStatusListener listener) { listeners.add(listener); }
    public List<Nutrient> getScheduledAlarms() { return scheduledAlarms; }
}