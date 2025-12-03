package com.nutrient_reminder.controller;

import com.nutrient_reminder.model.Nutrient; // [중요] Nutrient 모델 사용
import com.nutrient_reminder.service.AlarmSchedulerService;
import com.nutrient_reminder.service.AlarmSchedulerService.AlarmStatusListener;
import com.nutrient_reminder.service.UserSession;
import javafx.application.Platform; // [복구] import 추가
import javafx.event.ActionEvent;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainController implements AlarmAddPopupController.AlarmSaveListener, AlarmStatusListener {

    // 서비스 인스턴스 (연동용)
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

        // 💡 1. MainController를 알람 상태 변화 리스너로 등록
        service.addListener(this);

        // 💡 2. 저장된 알람을 불러와 UI에 표시하는 로직
        loadAlarms();
    }

    private void loadAlarms() {
        alarmListContainer.getChildren().clear();
        String currentUserId = UserSession.getUserId();
        String todayKorean = service.getTodayKorean();

        for (Nutrient alarm : service.getScheduledAlarms()) {
            if (currentUserId != null && !currentUserId.equals(alarm.getUserId())) continue;

            // 요일 정보 포맷
            String dateText = alarm.getDays().isEmpty()
                    ? "반복 없음"
                    : String.join(", ", alarm.getDays()) + "요일 (매주 반복)";

            String timeTextRaw = alarm.getTime().replaceAll("오전|오후", "").trim();
            String timeText = timeTextRaw.replaceAll(" : ", ":");

            // 오늘 알람인지 확인 (필터링하지 않고 변수에 담음)
            boolean isToday = alarm.getDays().isEmpty() || alarm.getDays().contains(todayKorean);

            // addAlarmToUI 호출 (isToday 플래그 추가)
            addAlarmToUI(dateText, timeText, alarm.getName(), alarm.getTime(), alarm.getId(), alarm.getStatus(), isToday);
        }
    }

    // AlarmAddPopupController.AlarmSaveListener 인터페이스 구현 (팝업 데이터 수신)
    @Override
    public void onAlarmSaved(String name, List<String> days, String time) {
        String userId = UserSession.getUserId();

        // 서비스에 알람 등록 요청 (userId 포함)
        service.registerAlarm(userId, name, time, days, null);

        // 화면 갱신
        loadAlarms();
    }

    // 알림박스 메소드 (깔끔한 디자인 및 두 개의 버튼, ID 추가)
    public void addAlarmToUI(String dateText, String timeText, String pillName, String subTime, String alarmId, String status, boolean isToday) {

        // 💡 디자인 개선: 흰색 배경, 부드러운 회색 테두리 그림자 추가
        VBox alarmBox = new VBox();
        alarmBox.setId(alarmId); // 💡 VBox에 알람 ID 설정 (상태 변경 시 검색용)
        alarmBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.0, 0, 3);");
        alarmBox.setPadding(new Insets(15, 20, 15, 20)); // 안쪽 여백
        alarmBox.setSpacing(10); // 내부 요소 간격

        // [추가] 오늘 알람이 아니거나, 이미 먹었으면 흐리게 처리
        if (!isToday || "COMPLETED".equals(status)) {
            alarmBox.setOpacity(0.5);
            alarmBox.setDisable(true);
        }

        // 날짜 라벨
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #999999; -fx-font-size: 14px;");

        // 시간 & 약 이름 ( 가로 배치 )
        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER_LEFT); // 왼쪽 정렬
        contentBox.setSpacing(50); // 시간과 약 이름 사이 간격

        // 시간 ( ex 8시 30분 )
        Label mainTimeLabel = new Label(timeText);
        mainTimeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 약 이름 ( ex 비타민 c )
        Label pillLabel = new Label(pillName);
        pillLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 💡 옵션 버튼 ( ... ) 추가
        Button optionButton = new Button("···");
        optionButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 24px; -fx-cursor: hand;");

        // 약 이름과 옵션 버튼 사이에 공간을 채우기 위해 Pane 추가
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // contentBox 조립
        contentBox.getChildren().addAll(mainTimeLabel, pillLabel, spacer, optionButton);

        // 💡 하단 버튼 HBox
        HBox buttonBar = new HBox();
        buttonBar.setSpacing(10); // 버튼 간격
        buttonBar.setAlignment(Pos.CENTER);

        String btnStyle = "-fx-background-color: #E8F5FF; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;";

        Button eatenButton = new Button("먹었습니다");
        eatenButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(eatenButton, Priority.ALWAYS);
        eatenButton.setUserData(alarmId);
        eatenButton.setStyle(btnStyle);
        eatenButton.setOnAction(this::handleAlarmAction);

        Button snoozeButton = new Button("30분 뒤 다시 울림");
        snoozeButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(snoozeButton, Priority.ALWAYS);
        snoozeButton.setUserData(alarmId);
        snoozeButton.setStyle(btnStyle);
        snoozeButton.setOnAction(this::handleAlarmAction);

        buttonBar.getChildren().addAll(eatenButton, snoozeButton);
        alarmBox.getChildren().addAll(dateLabel, contentBox, buttonBar);

        if (alarmListContainer != null) {
            alarmListContainer.getChildren().add(alarmBox);
        }
    }

    private void handleAlarmAction(ActionEvent event) {
        Button source = (Button) event.getSource();
        String action = source.getText();
        String alarmId = (String) source.getUserData();

        if ("먹었습니다".equals(action)) {
            service.updateAlarmStatus(alarmId, "COMPLETED");
        } else if ("30분 뒤 다시 울림".equals(action)) {
            service.updateAlarmStatus(alarmId, "SNOOZED");
        }
    }

    // [추가] 자정이 지났을 때 호출 (화면 새로고침)
    @Override
    public void onDateChanged() {
        System.out.println("메인 화면: 자정이 지나 화면을 갱신합니다.");
        Platform.runLater(this::loadAlarms);
    }

    @Override
    public void onAlarmStatusChanged(String alarmId, String newStatus) {
        for (Node node : alarmListContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox alarmBox = (VBox) node;
                if (alarmId.equals(alarmBox.getId())) {
                    if ("COMPLETED".equals(newStatus)) {
                        // [변경] 삭제하지 않고 흐리게 처리
                        alarmBox.setOpacity(0.5);
                        alarmBox.setDisable(true);
                        return;
                    }
                }
            }
        }
    }

    // --- 기존 메서드 유지 ---
    @FXML
    private void handleLogout() {
        try {
            UserSession.clear();
            Parent root = FXMLLoader.load(getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();

            // Root 교체 방식으로 변경
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecommendTab() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/nutrient_reminder/view/nutrient-check.fxml"));
            Stage stage = (Stage) recommendTabButton.getScene().getWindow();

            // Root 교체 방식으로 변경
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
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

    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.98); node.setScaleY(0.98);
    }

    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(1.0); node.setScaleY(1.0);
    }
}