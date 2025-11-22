package com.nutrient_reminder.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button mainTabButton;

    @FXML
    private Button recommendTabButton;

    @FXML
    private Button addButton;

    // 로그인한 아이디를 받아 화면에 표시하는 메서드
    public void setUsername(String username) {
        userNameLabel.setText(username + " 님");
    }

    @FXML
    public void initialize() {
        System.out.println("메인 화면이 초기화되었습니다.");
    }

    // 로그아웃 + 로그인 화면으로 이동
    @FXML
    private void handleLogout() {
        System.out.println("로그아웃 버튼 클릭됨");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/login.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("로그인");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 알람 팝업 띄우기
    @FXML
    private void handleAdd() {
        System.out.println("추가(+) 버튼 클릭됨");

        try {
            // 팝업 FXML 로드
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/alarmAddPopup.fxml")

            );
            Parent root = loader.load();

            // 팝업용 Stage 생성
            Stage popupStage = new Stage();
            popupStage.initOwner(userNameLabel.getScene().getWindow()); // 부모창 설정
            popupStage.initModality(Modality.WINDOW_MODAL);             // 부모창 비활성화
            popupStage.setTitle("알람 추가");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
