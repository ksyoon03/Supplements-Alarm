package com.nutrient_reminder.controller;

import com.nutrient_reminder.SupplementRecommenderModel; // 모델 위치 확인 필요
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class RecommendationPopup {

    public static void show(List<String> selectedSymptoms) {
        // 1. 새 창(Stage) 생성
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // 이 창을 닫기 전까지 뒤에 창 클릭 불가
        popupStage.setTitle("추천 영양 성분 결과");

        // 2. 모델을 통해 추천 결과 가져오기
        SupplementRecommenderModel model = new SupplementRecommenderModel();
        List<String> results = model.getRecommendations(selectedSymptoms);

        // 3. 결과를 보여줄 TextArea 생성 (글자가 많을 수 있으니 Label 대신 TextArea 사용)
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false); // 수정 불가능하게 설정
        resultArea.setWrapText(true);  // 줄 바꿈 허용
        resultArea.setFont(Font.font("Malgun Gothic", 14)); // 한글 폰트 설정

        // 결과 리스트를 하나의 문자열로 합치기
        StringBuilder content = new StringBuilder();
        if (results.isEmpty()) {
            content.append("선택된 증상이 없거나, 추천할 영양제가 없습니다.\n증상을 선택해주세요.");
        } else {
            for (String line : results) {
                content.append(line).append("\n");
            }
        }
        resultArea.setText(content.toString());

        // 4. 닫기 버튼 생성
        Button closeButton = new Button("닫기");
        closeButton.setOnAction(e -> popupStage.close());
        closeButton.setMaxWidth(Double.MAX_VALUE); // 버튼 꽉 채우기

        // 5. 레이아웃 구성 (VBox: 세로로 배치)
        VBox layout = new VBox(10); // 간격 10
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(resultArea, closeButton);

        // 6. 씬(Scene) 설정 및 보여주기
        Scene scene = new Scene(layout, 400, 500); // 창 크기 (가로 400, 세로 500)
        popupStage.setScene(scene);
        popupStage.showAndWait(); // 창이 닫힐 때까지 대기
    }
}