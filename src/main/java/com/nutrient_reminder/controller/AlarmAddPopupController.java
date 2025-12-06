package com.nutrient_reminder.controller;

import com.nutrient_reminder.model.Nutrient;
import com.nutrient_reminder.service.AlarmSchedulerService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlarmAddPopupController {

    public interface AlarmSaveListener {
        void onAlarmSaved(String name, List<String> days, String time, String idToUpdate);
    }

    private AlarmSaveListener listener;
    private String idToUpdate = null; // 수정 모드일 경우 ID 저장 변수

    // 서비스 인스턴스 (충돌 체크용)
    private final AlarmSchedulerService service = AlarmSchedulerService.getInstance();

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

        // 12시간제로 변환
        int currentHour12 = now.getHour() % 12;
        if (currentHour12 == 0) currentHour12 = 12;

        ampmLabel.setText(now.getHour() < 12 ? "오전" : "오후");
        hourField.setText(String.format("%02d", currentHour12));
        minuteField.setText(String.format("%02d", now.getMinute()));

        restrictToNumbers(hourField, 1, 12);
        restrictToNumbers(minuteField, 0, 59);

        // 저장 버튼에 마우스 동적 효과 연결
        if (saveButton != null) {
            saveButton.setOnMouseEntered(this::onButtonHoverEnter);
            saveButton.setOnMouseExited(this::onButtonHoverExit);
            saveButton.setOnMousePressed(this::onButtonPress);
            saveButton.setOnMouseReleased(this::onButtonRelease);
        }
    }

    // 기능 추가 수정 모드일 때 기존 데이터를 팝업에 채워 넣는 메서드
    public void setEditData(Nutrient nutrient) {
        this.idToUpdate = nutrient.getId(); // 수정할 ID 저장
        this.nameField.setText(nutrient.getName());

        // 시간 파싱 (예: "오전 09 : 30") -> 화면에 분리해서 표시
        String[] parts = nutrient.getTime().split("[:\\s]+");
        if (parts.length >= 3) {
            ampmLabel.setText(parts[0]);
            hourField.setText(parts[1]);
            minuteField.setText(parts[2]);
        }

        // 요일 버튼 상태 설정
        List<String> days = nutrient.getDays();
        sunToggle.setSelected(days.contains("일"));
        monToggle.setSelected(days.contains("월"));
        tueToggle.setSelected(days.contains("화"));
        wedToggle.setSelected(days.contains("수"));
        thuToggle.setSelected(days.contains("목"));
        friToggle.setSelected(days.contains("금"));
        satToggle.setSelected(days.contains("토"));

        saveButton.setText("수정"); // 버튼 글자를 '수정'으로 변경
    }


    public void setAlarmSaveListener(AlarmSaveListener listener) {
        this.listener = listener;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String hourStr = hourField.getText().trim();
        String minuteStr = minuteField.getText().trim();
        List<String> days = new ArrayList<>();

        // 유효성 검사
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "약 이름을 입력해 주세요.");
            return;
        }
        if (hourStr.isEmpty() || minuteStr.isEmpty() || hourStr.length() > 2 || minuteStr.length() > 2) {
            showAlert(Alert.AlertType.WARNING, "입력 오류", "시간을 정확히 입력해 주세요.");
            return;
        }

        if (sunToggle.isSelected()) days.add("일");
        if (monToggle.isSelected()) days.add("월");
        if (tueToggle.isSelected()) days.add("화");
        if (wedToggle.isSelected()) days.add("수");
        if (thuToggle.isSelected()) days.add("목");
        if (friToggle.isSelected()) days.add("금");
        if (satToggle.isSelected()) days.add("토");

        String ampm = ampmLabel.getText();
        String time = String.format("%s %s : %s", ampm, hourStr, minuteStr);

        // 충돌 감지 로직
        // 1. 서비스에 충돌 여부를 물어봄
        String conflictMsg = service.checkConflict(name, time);

        // 2. 충돌 메시지가 돌아오면(null이 아니면) 경고 팝업 띄움
        if (conflictMsg != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("⚠️ 성분 충돌 경고");
            alert.setHeaderText("함께 복용 시 주의가 필요해요!");
            alert.setContentText(conflictMsg + "\n\n그래도 저장하시겠습니까?");

            // 버튼 커스텀 (예/아니요)
            ButtonType yesButton = new ButtonType("예 (저장)");
            ButtonType noButton = new ButtonType("아니요 (취소)", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();

            // '아니요'를 누르거나 창을 닫으면 저장하지 않고 리턴
            if (result.isEmpty() || result.get() != yesButton) {
                return;
            }
        }

        // 리스너 호출 (수정된 ID 전달)
        if (listener != null) {
            listener.onAlarmSaved(name, days, time, idToUpdate);
        }

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    // --- 유틸리티 및 UI 이벤트 ---

    @FXML
    private void toggleAmPm(MouseEvent event) {
        if ("오전".equals(ampmLabel.getText())) ampmLabel.setText("오후");
        else ampmLabel.setText("오전");
    }

    private void restrictToNumbers(TextField field, int minVal, int maxVal) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }
            if (!field.getText().isEmpty()) {
                try {
                    int value = Integer.parseInt(field.getText());
                    if (value < minVal) {
                        if (minVal > 0 && value == 0) field.setText(String.format("%02d", minVal));
                    } else if (value > maxVal) {
                        field.setText(String.valueOf(maxVal));
                    }
                } catch (NumberFormatException e) { }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("알림");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 버튼 마우스 이벤트 핸들러
    @FXML private void onButtonHoverEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #567889; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 20px;");
        button.setScaleX(1.02); button.setScaleY(1.02);
    }
    @FXML private void onButtonHoverExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #D0E8F2; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 20px;");
        button.setScaleX(1.0); button.setScaleY(1.0);
    }
    @FXML private void onButtonPress(MouseEvent event) {
        Node node = (Node) event.getSource(); node.setScaleX(0.98); node.setScaleY(0.98);
    }
    @FXML private void onButtonRelease(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #D0E8F2; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 20px;");
        button.setScaleX(1.0); button.setScaleY(1.0);
    }
}