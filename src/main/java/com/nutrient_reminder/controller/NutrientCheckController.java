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
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane; // [추가됨] FlowPane 임포트
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.*;
import java.net.URL;

import javafx.scene.input.MouseEvent;
import javafx.scene.Node;

// 증상 자가 진단 및 영양제 추천 화면을 제어하는 컨트롤러
// - 증상 체크박스 동적 생성 (초성별 그룹화)
// - 추천 버튼 클릭 시 결과 도출 및 화면 표시
public class NutrientCheckController {

    @FXML private GridPane checkboxGrid;       // 증상 체크박스들이 들어갈 그리드
    @FXML private FlowPane resultContainer;    // 추천 결과 아이콘들이 나열될 공간
    @FXML private VBox resultBox;              // 결과 영역 전체 컨테이너
    @FXML private Label recommendationTitle;   // 제목 라벨
    @FXML private Label userLabel;             // 상단 사용자 ID 표시
    @FXML private Button mainTabButton;        // 메인 탭 이동 버튼

    // 에러 메시지 등을 띄우기 위한 공통 팝업 메서드
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        // 1. 사용자 ID 표시 설정
        String currentId = UserSession.getUserId();
        if (userLabel != null && currentId != null) {
            userLabel.setText("'" + currentId + "' 님");
        }

        try {
            // 2. 모델에서 모든 증상 목록 가져오기
            List<String> symptoms = SupplementRecommenderModel.getAllSymptoms();

            // 3. 증상을 초성(ㄱ, ㄴ, ㄷ...)별로 그룹화하여 맵에 저장
            Map<Character, List<String>> groupedSymptoms = groupByInitialConsonant(symptoms);

            // 4. 그룹화된 데이터로 체크박스 UI 동적 생성 (한 줄에 6개씩 배치)
            createCheckboxes(groupedSymptoms, 6);
        } catch (Exception e) {
            System.err.println("증상 목록을 불러오는 중 오류 발생: " + e.getMessage());
            showAlert("데이터 로드 오류", "증상 목록을 불러오는 중 오류가 발생했습니다.");
        }

        // 5. 초기에는 결과 박스를 숨김 처리
        if (resultBox != null) {
            resultBox.setVisible(false);
            resultBox.setManaged(false); // 레이아웃 공간도 차지하지 않도록 설정
        }
    }

    // 초성별 헤더와 체크박스를 그리드에 배치하는 로직
    private void createCheckboxes(Map<Character, List<String>> groupedSymptoms, int columns) {
        int row = 0;
        char[] consonants = {'ㄱ', 'ㄴ', 'ㄷ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅅ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

        for (char consonant : consonants) {
            if (!groupedSymptoms.containsKey(consonant)) continue;

            // (1) 초성 헤더 생성 (예: 'ㄱ', 'ㄴ')
            Label header = new Label(String.valueOf(consonant));
            header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a90e2;");
            checkboxGrid.add(header, 0, row++);
            GridPane.setColumnSpan(header, columns); // 헤더는 가로 전체를 차지

            // (2) 해당 초성에 속하는 증상 체크박스 생성
            List<String> symptomList = groupedSymptoms.get(consonant);
            for (int i = 0; i < symptomList.size(); i++) {
                CheckBox checkBox = new CheckBox(symptomList.get(i));
                checkBox.setStyle("-fx-font-size: 13px;");

                // 그리드 좌표 계산 (col: 열, row: 행)
                int col = i % columns;
                if (col == 0 && i > 0) row++; // 컬럼 꽉 차면 다음 줄로
                checkboxGrid.add(checkBox, col, row);
            }
            row++; // 다음 초성 그룹을 위해 줄 바꿈
        }
    }

    // 증상 리스트를 받아 초성별로 분류하는 헬퍼 메서드
    private Map<Character, List<String>> groupByInitialConsonant(List<String> symptoms) {
        Map<Character, List<String>> grouped = new LinkedHashMap<>();
        for (String symptom : symptoms) {
            char initial = getInitialConsonant(symptom.charAt(0));
            grouped.computeIfAbsent(initial, k -> new ArrayList<>()).add(symptom);
        }
        return grouped;
    }

    // 한글 유니코드를 분석하여 초성을 추출하는 로직
    private char getInitialConsonant(char ch) {
        if (ch >= '가' && ch <= '힣') {
            int unicode = ch - '가';
            int initialIndex = unicode / (21 * 28); // 초성 인덱스 계산
            char[] initials = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
            return initials[initialIndex];
        }
        return ch; // 한글이 아니면 그대로 반환
    }

    // [핵심 기능] "추천 받기" 버튼 클릭 시 실행
    @FXML
    private void onRecommendClick() {
        // 1. 기존 결과 화면 초기화
        resultContainer.getChildren().clear();

        // 2. 체크된 체크박스 확인하여 증상 리스트 수집
        List<String> selectedSymptoms = new ArrayList<>();
        for (Node node : checkboxGrid.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) node;
                if (checkBox.isSelected()) {
                    selectedSymptoms.add(checkBox.getText());
                }
            }
        }

        // 3. 예외 처리: 선택된 증상이 없을 경우
        if (selectedSymptoms.isEmpty()) {
            resultBox.setVisible(false);
            resultBox.setManaged(false);
            showAlert("증상 선택 필요", "증상을 하나 이상 선택해주세요!");
            return;
        }

        // 4. 모델을 통해 추천 영양성분 데이터 가져오기
        SupplementRecommenderModel model = new SupplementRecommenderModel();
        Set<String> recommendedNutrients = model.getRecommendedNutrients(selectedSymptoms);

        // 5. 결과 없음 처리
        if (recommendedNutrients.isEmpty()) {
            resultBox.setVisible(false);
            resultBox.setManaged(false);
            showAlert("결과 없음", "해당 증상에 대한 추천 데이터가 없습니다.");
            return;
        }

        // 6. 결과 화면 구성
        // 6-1. 제목 설정
        String userId = UserSession.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "회원";
        }
        recommendationTitle.setText(userId + "님에게 추천하는 영양 성분");

        // 6-2. 결과 박스 표시
        resultBox.setVisible(true);
        resultBox.setManaged(true);

        // 6-3. 추천된 영양성분별 아이콘 및 라벨 생성하여 추가
        for (String nutrientName : recommendedNutrients) {
            VBox itemBox = new VBox(10); // 아이콘 + 텍스트를 세로로 배치
            itemBox.setAlignment(Pos.TOP_CENTER);

            // (A) 원형 이미지 생성
            String imageFileName = model.getNutrientImage(nutrientName);
            ImageView imageView = createCircularImageView(imageFileName);

            // 이미지 클릭 시 상세 팝업 오픈
            imageView.setOnMouseClicked(e -> RecommendationPopup.display(nutrientName));
            imageView.setStyle("-fx-cursor: hand;");

            // (B) 영양성분 이름 라벨 생성
            Label nameLabel = new Label(nutrientName);
            nameLabel.setWrapText(true);
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            nameLabel.setMaxWidth(100);

            // 라벨 클릭 시에도 상세 팝업 오픈
            nameLabel.setOnMouseClicked(e -> RecommendationPopup.display(nutrientName));
            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-cursor: hand;");

            itemBox.getChildren().addAll(imageView, nameLabel);
            resultContainer.getChildren().add(itemBox); // FlowPane에 추가 (자동 줄바꿈됨)
        }
    }

    // (사용 안 함) 결과 없을 때 텍스트 표시용 헬퍼
    private void showNoResultLabel(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #999999; -fx-font-size: 14px;");
        resultContainer.getChildren().add(label);
    }

    // 이미지를 원형으로 잘라서 반환하는 메서드
    private ImageView createCircularImageView(String imageFileName) {
        ImageView imageView = new ImageView();
        try {
            URL imageUrl = getClass().getResource("/images/" + imageFileName);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                imageView.setImage(image);
            } else {
                System.err.println("이미지 파일을 찾을 수 없습니다: " + imageFileName);
            }
        } catch (Exception e) {
            System.err.println("이미지 로드 중 오류 발생: " + e.getMessage());
        }

        // 이미지 크기 설정
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        // 원형 클리핑 적용
        Circle clip = new Circle(40, 40, 40);
        imageView.setClip(clip);

        return imageView;
    }

    // 로그아웃 버튼 처리
    @FXML
    private void onLogoutClick() {
        // 현재 화면의 Stage 가져오기
        Stage stage = (Stage) checkboxGrid.getScene().getWindow();

        // 로그아웃 클래스 사용
        LogoutPopupController.handleLogout(stage);
    }

    // 상단 탭 버튼 클릭 처리
    @FXML
    private void handleMainTab() {
        onMainClick();
    }

    // 마우스 호버 효과 (버튼 작아짐)
    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.95);
        node.setScaleY(0.95);
    }

    // 마우스 호버 해제 (원래 크기)
    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }

    // 메인 화면 이동 로직
    @FXML
    private void onMainClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/main.fxml"));
            Parent root = loader.load();

            Scene currentScene = checkboxGrid.getScene();

            // 현재 Scene의 Root만 교체하여 화면 전환
            currentScene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("메인 화면으로 이동 실패");
        }
    }
}