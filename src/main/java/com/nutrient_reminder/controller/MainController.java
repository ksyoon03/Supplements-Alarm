package com.nutrient_reminder.controller;

import com.nutrient_reminder.model.Nutrient;
import com.nutrient_reminder.service.AlarmSchedulerService;
import com.nutrient_reminder.service.AlarmSchedulerService.AlarmStatusListener;
import com.nutrient_reminder.service.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController implements AlarmAddPopupController.AlarmSaveListener, AlarmStatusListener {

    private final AlarmSchedulerService service = AlarmSchedulerService.getInstance();

    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button mainTabButton;
    @FXML private Button recommendTabButton;
    @FXML private Button addButton;
    @FXML private VBox alarmListContainer;

    @FXML
    public void initialize() {
        String currentId = UserSession.getUserId();
        if (currentId != null) {
            userNameLabel.setText("'" + currentId + "' 님");
        }

        System.out.println("메인 화면이 초기화되었습니다.");
        service.addListener(this);
        loadAlarms();
    }

    private void loadAlarms() {
        alarmListContainer.getChildren().clear();
        String currentUserId = UserSession.getUserId();
        String todayKorean = service.getTodayKorean();

        for (Nutrient alarm : service.getScheduledAlarms()) {
            if (currentUserId != null && !currentUserId.equals(alarm.getUserId())) continue;

            String dateText = alarm.getDays().isEmpty()
                    ? "반복 없음"
                    : String.join(", ", alarm.getDays()) + "요일 (매주 반복)";

            String timeTextRaw = alarm.getTime().replaceAll("오전|오후", "").trim();
            String timeText = timeTextRaw.replaceAll(" : ", ":");

            boolean isToday = alarm.getDays().isEmpty() || alarm.getDays().contains(todayKorean);

            addAlarmToUI(dateText, timeText, alarm.getName(), alarm.getTime(), alarm.getId(), alarm.getStatus(), isToday, alarm);
        }
    }

    @Override
    public void onAlarmSaved(String name, List<String> days, String time, String idToUpdate) {
        String userId = UserSession.getUserId();

        if (idToUpdate == null) {
            service.registerAlarm(userId, name, time, days, null);
        } else {
            Nutrient updatedAlarm = new Nutrient(idToUpdate, userId, name, time, days, "ACTIVE");
            service.updateAlarm(updatedAlarm);
        }
        loadAlarms();
    }

    // 알림박스 메소드
    public void addAlarmToUI(String dateText, String timeText, String pillName, String subTime, String alarmId, String status, boolean isToday, Nutrient alarmData) {

        VBox alarmBox = new VBox();
        alarmBox.setId(alarmId);

        // 1. 배경 스타일
        String boxStyle = "-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.0, 0, 3);";
        alarmBox.setStyle(boxStyle);
        alarmBox.setPadding(new Insets(15, 20, 15, 20));
        alarmBox.setSpacing(10);

        // 2. 상태 표시
        boolean isCompleted = "COMPLETED".equals(status);
        // 오늘 약이 아니거나 이미 먹었으면 흐리게 처리
        if (!isToday || isCompleted) {
            alarmBox.setOpacity(0.5);
        }

        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #999999; -fx-font-size: 14px;");

        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.setSpacing(50);

        Label mainTimeLabel = new Label(timeText);
        mainTimeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label pillLabel = new Label(pillName);
        pillLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 3. 옵션 버튼 (...) - 항상 활성화 (수정/삭제 가능)
        Button optionButton = new Button("···");
        optionButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 24px; -fx-cursor: hand;");

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("수정");
        MenuItem deleteItem = new MenuItem("삭제");

        editItem.setOnAction(e -> openEditPopup(alarmData));
        deleteItem.setOnAction(e -> showDeleteConfirmation(alarmId));

        contextMenu.getItems().addAll(editItem, deleteItem);

        optionButton.setOnAction(e -> {
            contextMenu.show(optionButton, Side.BOTTOM, 0, 0);
        });

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        contentBox.getChildren().addAll(mainTimeLabel, pillLabel, spacer, optionButton);

        // 4. 하단 버튼 영역
        HBox buttonBar = new HBox();
        buttonBar.setSpacing(10);
        buttonBar.setAlignment(Pos.CENTER);

        String btnStyle = "-fx-background-color: #E8F5FF; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;";

        // 버튼 활성화 여부 결정
        boolean disableButtons = !isToday || isCompleted;

        Button eatenButton = new Button("먹었습니다");
        eatenButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(eatenButton, Priority.ALWAYS);
        eatenButton.setUserData(alarmId);
        eatenButton.setStyle(btnStyle);
        eatenButton.setDisable(disableButtons); // 상태에 따라 비활성화
        eatenButton.setOnAction(this::handleAlarmAction);
        setupButtonEvents(eatenButton);

        Button snoozeButton = new Button("30분 뒤 다시 울림");
        snoozeButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(snoozeButton, Priority.ALWAYS);
        snoozeButton.setUserData(alarmId);

        // 중복 클릭 방지
        if ("SNOOZED".equals(status)) {
            snoozeButton.setDisable(true);
        } else {
            snoozeButton.setDisable(disableButtons);
        }

        snoozeButton.setStyle(btnStyle);
        snoozeButton.setOnAction(this::handleAlarmAction);
        setupButtonEvents(snoozeButton);

        buttonBar.getChildren().addAll(eatenButton, snoozeButton);
        alarmBox.getChildren().addAll(dateLabel, contentBox, buttonBar);

        if (alarmListContainer != null) {
            alarmListContainer.getChildren().add(alarmBox);
        }
    }
    private void showDeleteConfirmation(String alarmId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("알람 삭제");
        alert.setHeaderText(null);
        alert.setContentText("정말 이 알람을 삭제하시겠습니까?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.deleteAlarm(alarmId);
        }
    }

    private void openEditPopup(Nutrient alarmData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/alarmAddPopup.fxml"));
            Parent root = loader.load();
            AlarmAddPopupController popupController = loader.getController();
            popupController.setAlarmSaveListener(this);
            popupController.setEditData(alarmData);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(userNameLabel.getScene().getWindow());
            popupStage.setTitle("알람 수정");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleAlarmAction(ActionEvent event) {
        Button source = (Button) event.getSource();
        String action = source.getText();
        String alarmId = (String) source.getUserData();

        if ("먹었습니다".equals(action)) service.updateAlarmStatus(alarmId, "COMPLETED");
        else if ("30분 뒤 다시 울림".equals(action)) service.updateAlarmStatus(alarmId, "SNOOZED");
    }

    @Override
    public void onDateChanged() {
        System.out.println("메인 화면: 자정이 지나 화면을 갱신합니다.");
        Platform.runLater(this::loadAlarms);
    }

    @Override
    public void onAlarmStatusChanged(String alarmId, String newStatus) {
        // 상태 변경 시 (스누즈 포함) 전체 갱신해서 색깔 바꿈
        loadAlarms();
    }

    // --- 기타 메뉴 및 이벤트 핸들러 ---
    @FXML private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("로그아웃 확인");
        alert.setHeaderText(null);
        alert.setContentText("정말 로그아웃 하시겠습니까?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserSession.clear();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) userNameLabel.getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setMaximized(true);
                stage.setTitle("로그인");
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML private void handleRecommendTab() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/nutrient_reminder/view/nutrient-check.fxml"));
            Stage stage = (Stage) recommendTabButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/alarmAddPopup.fxml"));
            Parent root = loader.load();
            AlarmAddPopupController popupController = loader.getController();
            popupController.setAlarmSaveListener(this);
            Stage popupStage = new Stage();
            popupStage.initOwner(userNameLabel.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.setTitle("알람 추가");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void onHoverEnter(MouseEvent event) { ((Node)event.getSource()).setScaleX(0.98); ((Node)event.getSource()).setScaleY(0.98); }
    @FXML private void onHoverExit(MouseEvent event) { ((Node)event.getSource()).setScaleX(1.0); ((Node)event.getSource()).setScaleY(1.0); }

    private void setupButtonEvents(Button btn) {
        btn.setOnMouseEntered(this::onAlarmButtonHoverEnter);
        btn.setOnMouseExited(this::onAlarmButtonHoverExit);
        btn.setOnMousePressed(this::onAlarmButtonPress);
        btn.setOnMouseReleased(this::onAlarmButtonRelease);
    }

    @FXML private void onAlarmButtonHoverEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        if (!button.isDisabled()) {
            button.setStyle("-fx-background-color: #567889; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
            button.setScaleX(1.02); button.setScaleY(1.02);
        }
    }
    @FXML private void onAlarmButtonHoverExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        if (!button.isDisabled()) {
            button.setStyle("-fx-background-color: #E8F5FF; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
            button.setScaleX(1.0); button.setScaleY(1.0);
        }
    }
    @FXML private void onAlarmButtonPress(MouseEvent event) {
        Node node = (Node) event.getSource();
        if (!node.isDisabled()) { node.setScaleX(0.98); node.setScaleY(0.98); }
    }
    @FXML private void onAlarmButtonRelease(MouseEvent event) {
        Button button = (Button) event.getSource();
        if (!button.isDisabled()) {
            button.setStyle("-fx-background-color: #D0E8F2; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
            button.setScaleX(1.0); button.setScaleY(1.0);
        }
    }
}