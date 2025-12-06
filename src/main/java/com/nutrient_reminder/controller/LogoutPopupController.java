package com.nutrient_reminder.controller;

import com.nutrient_reminder.service.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class LogoutPopupController {
    public static void handleLogout(Stage currentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("로그아웃");
        alert.setHeaderText(null);
        alert.setContentText("로그아웃 하시겠습니까?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 1. 세션 정보 삭제
                UserSession.clear();

                // 2. 로그인 화면 로드
                FXMLLoader loader = new FXMLLoader(LogoutPopupController.class.getResource("/com/nutrient_reminder/view/login-view.fxml"));
                Parent root = loader.load();

                // 3. 화면 전환
                Scene scene = currentStage.getScene();
                scene.setRoot(root);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("로그인 화면으로 이동 중 오류가 발생했습니다.");
            }
        }
    }
}
