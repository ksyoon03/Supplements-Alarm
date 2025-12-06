package com.nutrient_reminder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConflictPopupController {

    // [수정] 라벨을 두 개로 분리
    @FXML private Label warningLabel;
    @FXML private Label suggestionLabel;

    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    private boolean confirmed = false;

    // 외부에서 메시지를 넣어주는 메서드
    public void setConflictMessage(String fullMessage) {
        if (fullMessage == null) return;

        // 줄바꿈(\n)을 기준으로 최대 2개로 쪼갬
        String[] parts = fullMessage.split("\n", 2);

        if (parts.length > 0) {
            warningLabel.setText(parts[0].trim()); // 첫 줄 -> 주의사항
        }

        if (parts.length > 1) {
            suggestionLabel.setText(parts[1].trim()); // 둘째 줄 -> 섭취 제안
        } else {
            // 만약 줄바꿈이 없다면 제안 부분은 비워둠 (혹은 기본 멘트)
            suggestionLabel.setText("최소 2시간 이상의 간격을 두고 드시는 것을 권장합니다.");
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeStage();
    }

    @FXML
    private void handleConfirm() {
        confirmed = true;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}