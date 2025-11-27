package com.nutrient_reminder.controller;

import com.nutrient_reminder.model.Nutrient;
import com.nutrient_reminder.service.NutrientRepository;
import javafx.scene.control.Alert;

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

        try {
            Nutrient newNutrient = new Nutrient(name, timeLabel.getText(), days);
            NutrientRepository repository = new NutrientRepository();
            repository.save(newNutrient);

            showAlert("저장 완료", name + " 알림이 등록되었습니다!");

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("저장 실패", "오류가 발생했습니다.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}