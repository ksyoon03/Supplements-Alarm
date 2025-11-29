package com.nutrient_reminder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

// AWT 관련 import
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class Main extends Application {

    private static Stage primaryStageInstance;
    private TrayIcon trayIcon;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStageInstance = stage;

        // SystemTray 지원 시 AWT 연동 시작 (백그라운드 실행 기반)
        if (SystemTray.isSupported()) {
            try {
                setupSystemTray(stage);
            } catch (AWTException e) {
                System.err.println("AWT SystemTray 초기화 실패: " + e.getMessage());
            }
        }

        // FXML 로드 및 메인 스테이지 설정 (로그인 화면)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("영양제 알리미");
        stage.setScene(scene);
        stage.setMaximized(true);

        //  닫기 버튼을 눌렀을 때 윈도우를 숨기고 종료하지 않도록 설정
        stage.setOnCloseRequest(event -> {
            if (SystemTray.isSupported()) {
                event.consume(); // 기본 종료 이벤트 무시
                stage.hide();    // 창만 숨김
                // 트레이 알림 (아이콘이 null이 아닐 때만)
                if (trayIcon != null) {
                    trayIcon.displayMessage("영양제 알리미", "프로그램이 백그라운드에서 실행 중입니다.", TrayIcon.MessageType.INFO);
                }
            } else {
                Platform.exit();
            }
        });

        stage.show();
    }

    // AWT SystemTray 설정 메서드
    private void setupSystemTray(final Stage stage) throws AWTException {
        Platform.setImplicitExit(false);

        SystemTray tray = SystemTray.getSystemTray();

        // 아이콘 로드 (경로 오류 방지를 위해 임시 아이콘 사용 가능)
        URL iconURL = getClass().getResource("/com/nutrient_reminder/view/images/icon.png");
        Image image;

        if (iconURL == null) {
            image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        } else {
            image = Toolkit.getDefaultToolkit().getImage(iconURL);
        }

        trayIcon = new TrayIcon(image, "영양제 알리미");
        trayIcon.setImageAutoSize(true);

        // 메뉴 아이템 설정
        PopupMenu popup = new PopupMenu();

        // '보이기/숨기기' 메뉴
        MenuItem showItem = new MenuItem("보이기/숨기기");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            if (stage.isShowing()) {
                stage.hide();
            } else {
                stage.show();
                stage.toFront();
            }
        }));
        popup.add(showItem);

        // '종료' 메뉴
        MenuItem exitItem = new MenuItem("종료");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            Platform.exit();
            System.exit(0);
        });
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        tray.add(trayIcon);

        // 아이콘 더블클릭 이벤트 (윈도우 표시)
        trayIcon.addActionListener(e -> Platform.runLater(stage::show));
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getPrimaryStage() {
        return primaryStageInstance;
    }
}