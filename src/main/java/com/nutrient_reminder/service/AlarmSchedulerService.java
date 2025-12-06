package com.nutrient_reminder.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nutrient_reminder.controller.AlarmTriggerController;
import com.nutrient_reminder.model.Nutrient; // [중요] Nutrient 모델 사용
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
import java.util.List;
import java.util.Map; // Map import 추가
import java.util.HashMap; // HashMap import 추가
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors; // Collectors import 추가

public class AlarmSchedulerService {

    private static final String ALARM_FILE = "alarms_data.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 1. 성분 충돌 데이터베이스
    private static final Map<String, List<String>> CONFLICT_MAP = new HashMap<>();
    static {

    }

    // 인터페이스를 AlarmSchedulerService 클래스의 내부 (static public)로 정의
    public interface AlarmStatusListener {
        void onAlarmStatusChanged(String alarmId, String newStatus);
        void onDateChanged(); // [추가] 자정 체크용
    }

    private static AlarmSchedulerService instance;
    private List<AlarmStatusListener> listeners = new ArrayList<>();

    // 알람 데이터 저장소 역할 (Nutrient 객체를 저장)
    private final List<Nutrient> scheduledAlarms = new CopyOnWriteArrayList<>();

    // 1초마다 시간을 체크할 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 마지막으로 체크한 날짜 (자정 감지용)
    private LocalDate lastCheckDate = LocalDate.now();

    private AlarmSchedulerService() {
        // 싱글톤 패턴
        // 파일에서 저장된 알람 불러오기
        loadAlarmsFromFile();

        // 스케줄러 시작 (1초마다 시간 체크)
        startScheduler();
    }

    public static synchronized AlarmSchedulerService getInstance() {
        if (instance == null) {
            instance = new AlarmSchedulerService();
        }
        return instance;
    }

    // 1. 충돌 감지 메서드 구현
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
                        return String.format("주의: '%s'과(와) '%s'은(는) 함께 복용 시 흡수율이 떨어지거나 부작용이 있을 수 있습니다.", newName, alarm.getName());
                    }
                }
            }
        }
        return null;
    }

    // 2 알람 수정 메서드 구현
    public void updateAlarm(Nutrient updatedNutrient) {
        for (int i = 0; i < scheduledAlarms.size(); i++) {
            if (scheduledAlarms.get(i).getId().equals(updatedNutrient.getId())) {
                scheduledAlarms.set(i, updatedNutrient);
                break;
            }
        }
        saveAlarmsToFile();
        notifyListeners(updatedNutrient.getId(), "UPDATED");
    }

    // 3 알람 삭제 메서드 구현
    public void deleteAlarm(String alarmId) {

        scheduledAlarms.removeIf(alarm -> alarm.getId().equals(alarmId));
        saveAlarmsToFile();
        notifyListeners(alarmId, "DELETED");
    }


    // --- 스케줄러 로직 ---
    private void startScheduler() {
        scheduler.scheduleAtFixedRate(this::checkAlarmTime, 0, 1, TimeUnit.SECONDS);
    }

    private void checkAlarmTime() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        // 1. 자정(날짜 변경) 체크
        if (!today.equals(lastCheckDate)) {
            lastCheckDate = today;
            Platform.runLater(() -> {
                for (AlarmStatusListener listener : listeners) listener.onDateChanged();
            });
        }

        String ampm = now.getHour() < 12 ? "오전" : "오후";
        int hour = now.getHour() % 12;
        if (hour == 0) hour = 12;
        String currentTimeStr = String.format("%s %02d : %02d", ampm, hour, now.getMinute());

        String currentUserId = UserSession.getUserId();
        if (currentUserId == null) return;

        for (Nutrient alarm : scheduledAlarms) {
            if (!currentUserId.equals(alarm.getUserId())) continue;

            // 자정 초기화 로직
            if (!today.toString().equals(alarm.getLastTakenDate()) && "COMPLETED".equals(alarm.getStatus())) {
                alarm.setStatus("ACTIVE");
            }

            if (alarm.getTime().equals(currentTimeStr) && "ACTIVE".equals(alarm.getStatus())) {
                // 0초에 한 번만 실행
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

    private void notifyListeners(String alarmId, String status) {
        Platform.runLater(() -> {
            for (AlarmStatusListener listener : listeners) {
                listener.onAlarmStatusChanged(alarmId, status);
            }
        });
    }

    public void addListener(AlarmStatusListener listener) {
        listeners.add(listener);
        System.out.println("MainController가 AlarmSchedulerService에 등록되었습니다.");
    }

    public List<Nutrient> getScheduledAlarms() {
        return scheduledAlarms;
    }

    public Nutrient registerAlarm(String userId, String name, String time, List<String> days, String alarmId) {
        if (alarmId == null) alarmId = "alarm_" + System.currentTimeMillis();

        Nutrient newAlarm = new Nutrient(alarmId, userId, name, time, days, "ACTIVE");
        scheduledAlarms.add(newAlarm);

        saveAlarmsToFile();
        return newAlarm;
    }

    public void updateAlarmStatus(String alarmId, String status) {
        for (Nutrient alarm : scheduledAlarms) {
            if (alarm.getId().equals(alarmId)) {
                if ("COMPLETED".equals(status)) {
                    alarm.setStatus("COMPLETED");
                    alarm.setLastTakenDate(LocalDate.now().toString());
                }
                // [추가] 스누즈 상태도 저장해야 화면에 반영됨!
                else if ("SNOOZED".equals(status)) {
                    alarm.setStatus("SNOOZED");
                    // (심화 기능: 실제 시간을 30분 뒤로 바꾸는 로직은 나중에 추가 가능)
                }
            }
        }
        saveAlarmsToFile(); // 변경 사항 저장

        // 모든 리스너(MainController)에게 변경 사실 통보
        Platform.runLater(() -> {
            for (AlarmStatusListener listener : listeners) {
                listener.onAlarmStatusChanged(alarmId, status);
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
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}