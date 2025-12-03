package com.nutrient_reminder.controller;

import com.nutrient_reminder.service.AlarmSchedulerService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AlarmTriggerController {

    @FXML private Label timeLabel;
    @FXML private Label pillNameLabel;
    @FXML private Button offButton;
    @FXML private Button snoozeButton;

    private final AlarmSchedulerService service = AlarmSchedulerService.getInstance();
    private String alarmId;

    @FXML
    public void initialize() {

        offButton.setOnMouseEntered(this::onButtonHoverEnter);
        offButton.setOnMouseExited(this::onButtonHoverExit);
        offButton.setOnMousePressed(this::onButtonPress);
        offButton.setOnMouseReleased(this::onButtonRelease);

        snoozeButton.setOnMouseEntered(this::onButtonHoverEnter);
        snoozeButton.setOnMouseExited(this::onButtonHoverExit);
        snoozeButton.setOnMousePressed(this::onButtonPress);
        snoozeButton.setOnMouseReleased(this::onButtonRelease);
    }

    public void setAlarmInfo(String time, String pillName, String id) {
        timeLabel.setText(time);
        pillNameLabel.setText(pillName);
        this.alarmId = id;
    }

    @FXML
    private void handleOff() {
        service.updateAlarmStatus(alarmId, "COMPLETED");
        System.out.println("알람 끄기 요청 완료. ID: " + alarmId);
        closePopup();
    }

    @FXML
    private void handleSnooze() {
        service.updateAlarmStatus(alarmId, "SNOOZED");
        System.out.println("30분 뒤 스누즈 요청 완료. ID: " + alarmId);
        closePopup();
    }

    private void closePopup() {
        Stage stage = (Stage) offButton.getScene().getWindow();
        stage.close();
    }


    // 마우스 들어오면 ( 작아지기 )
    @FXML
    private void onButtonHoverEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #567889; -fx-background-radius: 5; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 18.0px;");
        button.setScaleX(1.02);
        button.setScaleY(1.02);
    }

    // 마우스가 버튼에서 나가면 ( 원래 크기 )
    @FXML
    private void onButtonHoverExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #D0E8F2; -fx-background-radius: 5; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 18.0px;");
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }

    // 버튼이 눌리면( 작아지기 )
    @FXML
    private void onButtonPress(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.98);
        node.setScaleY(0.98);
    }

    // 버튼에서 마우스를 떼면( 원래 크기 )
    @FXML
    private void onButtonRelease(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #D0E8F2; -fx-background-radius: 5; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 18.0px;");
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }
}