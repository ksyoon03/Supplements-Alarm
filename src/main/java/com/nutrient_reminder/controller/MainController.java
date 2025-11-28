package com.nutrient_reminder.controller;

import com.nutrient_reminder.service.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML
    private VBox alarmListContainer;

    @FXML
    public void initialize() {

        String currentId = UserSession.getUserId();
        if (currentId != null) {
            userNameLabel.setText("'" + currentId + "' 님");
        }

        System.out.println("메인 화면이 초기화되었습니다.");

        // 테스트 용
        addAlarmToUI("11월 06일", "08:00", "섭취중인 약", "08:00");
        addAlarmToUI("11월 07일", "13:00", "비타민 C", "13:00");
    }

    // 알림박스 메소드
    public void addAlarmToUI(String dateText, String timeText, String pillName, String subTime) {

        // 파란색 배경 박스
        VBox alarmBox = new VBox();
        alarmBox.setStyle("-fx-background-color: #EAF6FA; -fx-background-radius: 15;");
        alarmBox.setPadding(new Insets(15, 20, 15, 20)); // 안쪽 여백
        alarmBox.setSpacing(10); // 내부 요소 간격

        // 날짜 라벨
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555; -fx-font-size: 14px;");

        // 시간 & 약 이름 ( 가로 배치 )
        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER); // 가운데 정렬
        contentBox.setSpacing(50); // 시간과 약 이름 사이 간격

        // 시간 ( ex 8시 30분 )
        Label mainTimeLabel = new Label(timeText);
        mainTimeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 약 이름 ( ex 비타민 c )
        Label pillLabel = new Label(pillName);
        pillLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555555;");

        contentBox.getChildren().addAll(mainTimeLabel, pillLabel);

        // 하단 바 ( 버튼 )
        Label bottomBar = new Label(subTime);
        bottomBar.setMaxWidth(Double.MAX_VALUE);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(5, 0, 5, 0));
        bottomBar.setStyle("-fx-background-color: #D0E8F2; -fx-background-radius: 10; -fx-text-fill: #667880; -fx-font-weight: bold; -fx-font-style: italic;");

        // 박스 조립
        alarmBox.getChildren().addAll(dateLabel, contentBox, bottomBar);

        // 실제 화면(리스트 통)에 추가하기
        if (alarmListContainer != null) {
            alarmListContainer.getChildren().add(alarmBox);
        } else {
            System.out.println("오류: alarmListContainer가 연결되지 않았습니다. fxml의 fx:id를 확인하세요!");
        }
    }

    // 로그아웃
    @FXML
    private void handleLogout() {
        System.out.println("로그아웃 버튼 클릭됨");
        try {
            UserSession.clear();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/login-view.fxml")
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

    // 영양제 추천 탭 이동
    @FXML
    private void handleRecommendTab() {
        System.out.println("영양제 추천 탭 클릭됨");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/nutrient-check.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) recommendTabButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("영양제 추천");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("영양제 추천 화면으로 이동 실패");
        }
    }

    // 추가 팝업
    @FXML
    private void handleAdd() {
        System.out.println("추가(+) 버튼 클릭됨");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/alarmAddPopup.fxml")
            );
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initOwner(userNameLabel.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.setTitle("알람 추가");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 마우스 들어오면 ( 작아지기 )
    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.98);
        node.setScaleY(0.98);
    }

    // 마우스 나가면 ( 원래대로 )
    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }
}