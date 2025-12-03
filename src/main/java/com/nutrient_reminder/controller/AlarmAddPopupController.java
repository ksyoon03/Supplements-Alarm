package com.nutrient_reminder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert; // Alert 추가
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label; // Label 추가
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Optional 추가

public class AlarmAddPopupController {

    // 새로운 알람 정보를 메인 컨트롤러로 전달하기 위한 인터페이스 정의
    public interface AlarmSaveListener {
        // name: 약 이름, days: 선택된 요일 목록, time: "오전 09 : 30" 형식의 시간
        void onAlarmSaved(String name, List<String> days, String time);
    }

    private AlarmSaveListener listener;

    // 외부에서 MainController를 리스너로 설정할 수 있는 Setter 메서드
    public void setAlarmSaveListener(AlarmSaveListener listener) {
        this.listener = listener;
    }

    // FXML 필드
    @FXML private Label ampmLabel;
    @FXML private TextField hourField;
    @FXML private TextField minuteField;

    @FXML private TextField nameField;
    @FXML private Button saveButton;

    // 요일 선택 버튼들
    @FXML private ToggleButton sunToggle;
    @FXML private ToggleButton monToggle;
    @FXML private ToggleButton tueToggle;
    @FXML private ToggleButton wedToggle;
    @FXML private ToggleButton thuToggle;
    @FXML private ToggleButton friToggle;
    @FXML private ToggleButton satToggle;

    @FXML
    public void initialize() {
        LocalTime now = LocalTime.now();

        // 12시간제로 변환: 0시는 12시로, 13시 이후는 (시간 % 12)로 변환
        int currentHour12 = now.getHour() % 12;
        if (currentHour12 == 0) currentHour12 = 12;

        // AM/PM 초기 설정
        ampmLabel.setText(now.getHour() < 12 ? "오전" : "오후");

        // 시간 필드 초기 설정 (두 자리 포맷)
        hourField.setText(String.format("%02d", currentHour12));
        minuteField.setText(String.format("%02d", now.getMinute()));

        // 시간 필드에 숫자만 입력되도록 제한 및 범위 제한 로직 추가
        restrictToNumbers(hourField, 1, 12); // 12시간제 (1-12)
        restrictToNumbers(minuteField, 0, 59);
    }

    // AM/PM 토글 핸들러 (FXML에서 직접 연결)
    @FXML
    private void toggleAmPm(MouseEvent event) {
        if ("오전".equals(ampmLabel.getText())) {
            ampmLabel.setText("오후");
        } else {
            ampmLabel.setText("오전");
        }
    }

    // 숫자 및 범위 제한 로직 (기존 코드 유지)
    private void restrictToNumbers(TextField field, int minVal, int maxVal) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }
            if (!field.getText().isEmpty()) {
                try {
                    int value = Integer.parseInt(field.getText());
                    // 입력 값이 범위를 벗어날 경우 수정
                    if (value < minVal) {
                        // 0이 입력되었으나 minVal이 0보다 클 경우, minVal로 설정
                        if (minVal > 0 && value == 0) field.setText(String.format("%02d", minVal));
                    } else if (value > maxVal) {
                        field.setText(String.valueOf(maxVal));
                    }
                } catch (NumberFormatException e) {
                    // Do nothing
                }
            }
        });
    }

    // 공통 알림창 표시
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("알림");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    private void handleSave() {
        // 입력값 가져오기
        String name = nameField.getText().trim();
        String hourStr = hourField.getText().trim();
        String minuteStr = minuteField.getText().trim();
        List<String> days = new ArrayList<>();

        // 1. 유효성 검사 (약 이름)
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "약 이름을 입력해 주세요.");
            return;
        }

        // 2. 유효성 검사 (시간)
        if (hourStr.isEmpty() || minuteStr.isEmpty() || hourStr.length() > 2 || minuteStr.length() > 2) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "시간을 정확히 입력해 주세요.");
            return;
        }

        // 선택된 요일 리스트 만들기
        if (sunToggle.isSelected()) days.add("일");
        if (monToggle.isSelected()) days.add("월");
        if (tueToggle.isSelected()) days.add("화");
        if (wedToggle.isSelected()) days.add("수");
        if (thuToggle.isSelected()) days.add("목");
        if (friToggle.isSelected()) days.add("금");
        if (satToggle.isSelected()) days.add("토");

        // Time 형식 ex) "오전 09 : 30"
        String ampm = ampmLabel.getText();
        String time = String.format("%s %s : %s", ampm, hourStr, minuteStr);

        // 리스너를 통해 메인 컨트롤러에 정보를 전달
        if (listener != null) {
            listener.onAlarmSaved(name, days, time);
        } else {
            System.err.println("오류: AlarmSaveListener가 설정되지 않았습니다.");
        }

        // 팝업 닫기
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}