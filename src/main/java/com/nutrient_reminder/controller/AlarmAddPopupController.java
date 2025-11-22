package com.nutrient_reminder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlarmAddPopupController {

    @FXML private Label timeLabel;
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
        // 1. 현재 시간 가져오기
        LocalTime now = LocalTime.now();
        // 2. 한국어 포맷 적용 (예: 오전 09 : 30)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh : mm", Locale.KOREAN);
        timeLabel.setText(now.format(formatter));
    }

    @FXML
    private void handleSave() {
        // 입력값 가져오기
        String name = nameField.getText();

        // 선택된 요일 리스트 만들기
        List<String> days = new ArrayList<>();
        if (sunToggle.isSelected()) days.add("일");
        if (monToggle.isSelected()) days.add("월");
        if (tueToggle.isSelected()) days.add("화");
        if (wedToggle.isSelected()) days.add("수");
        if (thuToggle.isSelected()) days.add("목");
        if (friToggle.isSelected()) days.add("금");
        if (satToggle.isSelected()) days.add("토");

        // (확인용) 콘솔 출력
        System.out.println("=== 알람 저장 ===");
        System.out.println("약 이름: " + name);
        System.out.println("시간: " + timeLabel.getText());
        System.out.println("요일: " + days);

        // 팝업 닫기
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}