package com.nutrient_reminder.controller;

import com.nutrient_reminder.SupplementRecommenderModel;
import com.nutrient_reminder.service.UserSession; // ++ 전광판 사용

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

import java.io.IOException;
import java.util.*;

import javafx.scene.input.MouseEvent;
import javafx.scene.Node;

public class NutrientCheckController {

    @FXML
    private GridPane checkboxGrid;

    @FXML
    private Label userLabel;

    /*
    private String username;

    public void setUsername(String username) {
        this.username = username;
        if (userLabel != null) {
            userLabel.setText("'" + username + "' 님");
        }
    }
    */

    @FXML
    public void initialize() {
        // ++

        String currentId = UserSession.getUserId();
        if (userLabel != null && currentId != null) {
            userLabel.setText("'" + currentId + "' 님");
        }

        List<String> symptoms = SupplementRecommenderModel.getAllSymptoms();


        Map<Character, List<String>> groupedSymptoms = groupByInitialConsonant(symptoms);

        int row = 0;
        int columns = 4;

        // 초성 순서대로 출력
        char[] consonants = {'ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

        for (char consonant : consonants) {
            if (!groupedSymptoms.containsKey(consonant)) continue;

            // 초성 헤더 추가
            Label header = new Label(String.valueOf(consonant));
            header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a90e2;");
            checkboxGrid.add(header, 0, row++);
            GridPane.setColumnSpan(header, columns);

            // 해당 초성의 증상들 추가
            List<String> symptomList = groupedSymptoms.get(consonant);
            for (int i = 0; i < symptomList.size(); i++) {
                CheckBox checkBox = new CheckBox(symptomList.get(i));
                checkBox.setStyle("-fx-font-size: 13px;");

                int col = i % columns;
                if (col == 0 && i > 0) row++;
                checkboxGrid.add(checkBox, col, row);
            }
            row++;
        }
    }

    private Map<Character, List<String>> groupByInitialConsonant(List<String> symptoms) {
        Map<Character, List<String>> grouped = new LinkedHashMap<>();

        for (String symptom : symptoms) {
            char initial = getInitialConsonant(symptom.charAt(0));
            grouped.computeIfAbsent(initial, k -> new ArrayList<>()).add(symptom);
        }

        return grouped;
    }

    private char getInitialConsonant(char ch) {
        if (ch >= '가' && ch <= '힣') {
            int unicode = ch - '가';
            int initialIndex = unicode / (21 * 28);
            char[] initials = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
            return initials[initialIndex];
        }
        return ch;
    }

    @FXML
    private void onRecommendClick() {
        // 체크된 증상 수집
        List<String> selectedSymptoms = new ArrayList<>();

        for (javafx.scene.Node node : checkboxGrid.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    selectedSymptoms.add(checkBox.getText());
                }
            }
        }

        // 추천 팝업 표시
        RecommendationPopup.show(selectedSymptoms);
    }


    @FXML
    private void onLogoutClick() {
        // 확인 다이얼로그 생성
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("로그아웃");
        alert.setHeaderText(null);
        alert.setContentText("로그아웃 하시겠습니까?");

        // 버튼 텍스트 한글로 변경
        ButtonType yesButton = new ButtonType("예");
        ButtonType noButton = new ButtonType("아니요");
        alert.getButtonTypes().setAll(yesButton, noButton);

        // 사용자 응답 처리
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            // "예" 클릭 시 로그인 페이지로 이동
            try {
                // ++ 로그아웃 시 전광판 지우기
                UserSession.clear();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) checkboxGrid.getScene().getWindow();
                stage.setScene(new Scene(root, 750, 600));
                stage.setTitle("로그인"); // 창 제목 변경
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // "아니요" 클릭 시 팝업이 자동으로 닫힘
    }

    // ++ 마우스 액션 추가 ( 작아지기 )
    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.95); // 가로 0.95배
        node.setScaleY(0.95); // 세로 0.95배
    }

    // ++ 마우스 액션 추가 ( 원래대로 )
    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }

    // ++ 메인으로 이동
    @FXML
    private void onMainClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) checkboxGrid.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("영양제 알리미");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("메인 화면으로 이동 실패: 경로를 확인해주세요.");
        }
    }
}