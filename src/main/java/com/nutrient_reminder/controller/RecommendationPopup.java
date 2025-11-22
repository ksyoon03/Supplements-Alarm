package com.nutrient_reminder.controller;

import com.nutrient_reminder.SupplementRecommenderModel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class RecommendationPopup {

    public static void show(List<String> selectedSymptoms) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("영양제 추천 결과");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // 제목
        Label title = new Label("추천 영양제");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4a90e2;");
        content.getChildren().add(title);

        // 선택된 증상이 없을 경우
        if (selectedSymptoms.isEmpty()) {
            Label noSelection = new Label("증상을 선택해주세요.");
            noSelection.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            content.getChildren().add(noSelection);
        } else {
            // 추천 결과 가져오기
            SupplementRecommenderModel model = new SupplementRecommenderModel();
            List<String> recommendations = model.getRecommendations(selectedSymptoms);

            VBox recommendationList = new VBox(10);
            for (String recommendation : recommendations) {
                Label label = new Label(recommendation);
                label.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                label.setWrapText(true);
                recommendationList.getChildren().add(label);
            }

            ScrollPane scrollPane = new ScrollPane(recommendationList);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            scrollPane.setStyle("-fx-background-color: white;");
            content.getChildren().add(scrollPane);
        }

        // 닫기 버튼
        Button closeButton = new Button("닫기");
        closeButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 100; -fx-pref-height: 35;");
        closeButton.setOnAction(e -> popup.close());
        content.getChildren().add(closeButton);

        Scene scene = new Scene(content, 600, 500);
        popup.setScene(scene);
        popup.showAndWait();
    }
}
