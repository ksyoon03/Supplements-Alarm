package com.nutrient_reminder.service;

import com.nutrient_reminder.model.Nutrient;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // 스레드 안전한 리스트 사용

public class AlarmSchedulerService {

    // 인터페이스를 AlarmSchedulerService 클래스의 내부 (static public)로 정의
    public static interface AlarmStatusListener {
        void onAlarmStatusChanged(String alarmId, String newStatus);
    }

    private static AlarmSchedulerService instance;
    private List<AlarmStatusListener> listeners = new ArrayList<>();

    // 알람 데이터 저장소 역할 (Nutrient 객체를 저장)
    private final List<Nutrient> scheduledAlarms = new CopyOnWriteArrayList<>();

    private AlarmSchedulerService() {
        // 싱글톤 패턴
        // ~~ 테스트용 알람 초기화 (메인 화면 로딩 테스트를 위해 필요)
        initializeTestAlarms();
    }

    public static AlarmSchedulerService getInstance() {
        if (instance == null) {
            instance = new AlarmSchedulerService();
        }
        return instance;
    }

    // 리스너 등록 메서드 (MainController가 자신을 등록함)
    public void addListener(AlarmStatusListener listener) {
        listeners.add(listener);
        System.out.println("MainController가 AlarmSchedulerService에 등록되었습니다.");
    }

    // 알람 목록을 MainController에게 제공하는 메서드
    public List<Nutrient> getScheduledAlarms() {
        return scheduledAlarms;
    }

    // ~~ 테스트용 알람을 생성 및 저장하는 메서드 (MainController 로드 테스트용)
    private void initializeTestAlarms() {
        // 기존 알람 목록을 비우고 테스트 데이터 추가
        scheduledAlarms.clear();

        // 반복 알람: 비타민 C (매주 월, 수, 금) 오전 9:30
        scheduledAlarms.add(new Nutrient(
                "alarm_001",
                "비타민 C",
                "오전 09 : 30",
                List.of("월", "수", "금"),
                "ACTIVE"
        ));

        // [TEST 2] 일회성 알람 (반복 없음): 마그네슘 (반복 없음) 오후 8:00
        scheduledAlarms.add(new Nutrient(
                "alarm_002",
                "마그네슘",
                "오후 08 : 00",
                List.of(), // 빈 리스트 = 반복 없음
                "ACTIVE"
        ));

        System.out.println("서비스: 테스트 알람 2개 초기화 완료.");
    }

    // MainController의 onAlarmSaved에서 호출될 알람 등록 메서드
    public void registerAlarm(String name, String time, List<String> days, String alarmId) {
        // 새로운 알람 객체를 생성하고 저장소에 추가
        Nutrient newAlarm = new Nutrient(alarmId, name, time, days, "ACTIVE");
        scheduledAlarms.add(newAlarm);
        System.out.printf("서비스: 새로운 알람 등록됨 (ID: %s, 이름: %s)\n", alarmId, name);
        // 실제 스케줄링 로직은 추후 추가
    }


    // 알람 상태 변경 요청 처리 (AlarmTriggerController에서 호출됨)
    public void updateAlarmStatus(String alarmId, String status) {
        System.out.printf("서비스: 알람 ID %s의 상태를 %s로 변경 요청받음.\\n", alarmId, status);

        // 실제 알람 상태 변경 및 제거 로직
        if ("COMPLETED".equals(status)) {
            // 완료된 알람은 리스트에서 제거
            scheduledAlarms.removeIf(alarm -> alarmId.equals(alarm.getId()));
        } else {
            // 스누즈 등 상태만 변경
            scheduledAlarms.stream()
                    .filter(alarm -> alarmId.equals(alarm.getId()))
                    .findFirst()
                    .ifPresent(alarm -> alarm.setStatus(status)); // 상태만 업데이트
        }

        // 모든 리스너(MainController)에게 변경 사실 통보
        Platform.runLater(() -> {
            for (AlarmStatusListener listener : listeners) {
                listener.onAlarmStatusChanged(alarmId, status);
            }
        });
    }
}