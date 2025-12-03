package com.nutrient_reminder.controller;

import com.nutrient_reminder.SupplementRecommenderModel;
import com.nutrient_reminder.service.UserSession;

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
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.*;

import javafx.scene.input.MouseEvent;
import javafx.scene.Node;


public class NutrientCheckController {

    @FXML private GridPane checkboxGrid;
    @FXML private Label userLabel;
    @FXML private Button mainTabButton;

    // FXML에서 사용되지 않지만, 충돌 해결을 위해 필요한 메서드 (재정의)
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        String currentId = UserSession.getUserId();
        if (userLabel != null && currentId != null) {
            userLabel.setText("'" + currentId + "' 님");
        }

        // 증상 목록 로드 및 체크박스 생성
        try {
            List<String> symptoms = SupplementRecommenderModel.getAllSymptoms();
            Map<Character, List<String>> groupedSymptoms = groupByInitialConsonant(symptoms);

            // columns=4로 설정
            createCheckboxes(groupedSymptoms, 4);

        } catch (Exception e) {
            System.err.println("증상 목록을 불러오는 중 오류 발생: " + e.getMessage());
            showAlert("데이터 로드 오류", "증상 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    // 컬럼 수를 인자로 받는 체크박스 생성 메서드로 통합
    private void createCheckboxes(Map<Character, List<String>> groupedSymptoms, int columns) {
        int row = 0;
        char[] consonants = {'ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

        for (char consonant : consonants) {
            if (!groupedSymptoms.containsKey(consonant)) continue;

            Label header = new Label(String.valueOf(consonant));
            header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a90e2;");
            checkboxGrid.add(header, 0, row++);
            GridPane.setColumnSpan(header, columns);

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
        List<String> selectedSymptoms = new ArrayList<>();
        for (Node node : checkboxGrid.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    selectedSymptoms.add(checkBox.getText());
                }
            }
        }
        // RecommendationPopup 클래스는 별도 파일에 있다고 가정합니다.
        // RecommendationPopup.show(selectedSymptoms);
        System.out.println("추천 버튼 클릭됨. 선택된 증상: " + selectedSymptoms);
    }


    @FXML
    private void onLogoutClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("로그아웃");
        alert.setHeaderText(null);
        alert.setContentText("로그아웃 하시겠습니까?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserSession.clear();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));
                Parent root = loader.load();

                Scene currentScene = checkboxGrid.getScene();

                currentScene.setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 메인 탭 버튼에서 호출되는 액션 (FXML에 정의됨)
    @FXML
    private void handleMainTab() {
        onMainClick(); // onMainClick으로 통합
    }

    // ++ 마우스 액션 추가 ( 작아지기 )
    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.95);
        node.setScaleY(0.95);
    }

    // ++ 마우스 액션 추가 ( 원래대로 )
    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }

    // ++ 메인으로 이동 (onMainClick)
    @FXML
    private void onMainClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/main.fxml"));
            Parent root = loader.load();

            Scene currentScene = checkboxGrid.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // setScene 대신 setRoot 사용
            currentScene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("메인 화면으로 이동 실패");
        }
    }
}