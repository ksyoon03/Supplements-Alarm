package com.nutrient_reminder.controller;

import com.nutrient_reminder.SupplementRecommenderModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Set;


// 영양 성분 상세 정보를 보여주는 팝업창 클래스
// - 상세 설명, 섭취 가이드, 제품 구매 링크 등을 표시함
public class RecommendationPopup {

     // 상세 정보 팝업을 띄우는 메인 메서드
     // @param nutrientName 선택된 영양 성분 이름 (예: "비타민 C")
    public static void display(String nutrientName) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // 팝업 닫기 전까지 부모 창 조작 불가
        stage.setTitle(nutrientName + " 상세 정보");
        stage.setMinWidth(600);
        stage.setMinHeight(850);

        // 레이아웃 루트 설정 (CSS 클래스 적용)
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.TOP_LEFT);
        layout.getStyleClass().add("popup-root");

        // 1. 헤더 (제목 및 경고 문구)
        Label titleLabel = new Label(nutrientName);
        titleLabel.getStyleClass().add("title-label");

        Label warningLabel = new Label("반드시 섭취 전 전문가와 상담하세요!");
        warningLabel.getStyleClass().add("warning-label");

        // 2. 본문 컨텐츠 구성
        VBox contentBox = new VBox(20);

        // (1) 상세 설명 데이터 로드 및 추가
        String[] details = SupplementRecommenderModel.getNutrientDetails(nutrientName);
        if (details != null && details.length >= 2) {
            addSection(contentBox, "상세 설명", details[1], null);
        }

        // (2) 같이 먹으면 좋은 성분 (이미지 아이콘 형태로 표시)
        String good = SupplementRecommenderModel.getGoodCombo(nutrientName);
        if (good != null) {
            addVisualSection(contentBox, "같이 먹으면 좋은 성분", good, "good-header");
        }

        // (3) 같이 먹으면 안 좋은 성분
        String bad = SupplementRecommenderModel.getBadCombo(nutrientName);
        if (bad != null) {
            addVisualSection(contentBox, "같이 먹으면 안 좋은 성분", bad, "bad-header");
        }

        // (4) 섭취 시간 및 팁
        String time = SupplementRecommenderModel.getIntakeTip(nutrientName);
        if (time != null) {
            addSection(contentBox, "섭취 시간", time, null);
        }

        // (5) [제품 추천] 구매 링크와 이미지 표시
        // 모델에서 링크 리스트를 가져와서 섹션 추가
        String[] links = SupplementRecommenderModel.getNutrientLinks(nutrientName);
        if (links != null && links.length > 0) {
            addProductSection(contentBox, "제품 추천", links);
        }

        // 내용이 아예 없을 경우 대비
        if (contentBox.getChildren().isEmpty()) {
            contentBox.getChildren().add(new Label("상세 정보가 준비 중입니다."));
        }

        // 3. 스크롤 기능 추가
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("scroll-pane");

        // 4. 닫기 버튼 생성
        Button closeButton = new Button("닫기");
        closeButton.getStyleClass().add("close-btn");
        closeButton.setOnAction(e -> stage.close());

        VBox buttonBox = new VBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        // 전체 레이아웃 조합
        layout.getChildren().addAll(titleLabel, warningLabel, new Separator(), scrollPane, buttonBox);

        Scene scene = new Scene(layout, 600, 850);

        // CSS 파일 로드 (폰트 및 팝업 스타일)
        try {
            URL fontCssUrl = RecommendationPopup.class.getResource("/com/nutrient_reminder/view/style-sheets/font.css");
            if (fontCssUrl != null) scene.getStylesheets().add(fontCssUrl.toExternalForm());

            URL popupCssUrl = RecommendationPopup.class.getResource("/com/nutrient_reminder/view/style-sheets/recommendationPopup.css");
            if (popupCssUrl != null) scene.getStylesheets().add(popupCssUrl.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }

        stage.setScene(scene);
        stage.showAndWait();
    }

    // [헬퍼] 일반 텍스트 섹션 추가 (제목 + 내용)
    private static void addSection(VBox container, String title, String content, String colorStyle) {
        Label header = new Label(title);
        header.getStyleClass().add("section-header");
        if (colorStyle != null) header.getStyleClass().add(colorStyle);

        Label body = new Label(content);
        body.setWrapText(true);
        body.getStyleClass().add("section-body");

        container.getChildren().addAll(header, body, new Separator());
    }

    // [헬퍼] 텍스트 안에 포함된 영양성분을 감지하여 이미지 아이콘으로 보여주는 섹션
    private static void addVisualSection(VBox container, String title, String content, String colorStyle) {
        Label header = new Label(title);
        header.getStyleClass().add("section-header");
        if (colorStyle != null) header.getStyleClass().add(colorStyle);
        container.getChildren().add(header);

        HBox itemContainer = new HBox(20);
        itemContainer.setAlignment(Pos.TOP_LEFT);
        itemContainer.setPadding(new Insets(10, 0, 15, 0));

        Set<String> knownNutrients = SupplementRecommenderModel.getAllNutrientNames();
        String[] parts = content.split(",");

        boolean foundAny = false;
        // 텍스트를 분석하여 영양성분 이름이 있으면 아이콘 생성
        for (String part : parts) {
            String cleanPart = part.trim();
            for (String nutrient : knownNutrients) {
                if (cleanPart.contains(nutrient)) {
                    // 영양제 이름으로 이미지 찾기
                    VBox iconBox = createIconBox(nutrient, cleanPart, false, null);
                    itemContainer.getChildren().add(iconBox);
                    foundAny = true;
                    break;
                }
            }
        }

        // 아이콘을 만들었으면 가로 스크롤 패널에 넣기, 없으면 그냥 텍스트 출력
        if (foundAny) {
            ScrollPane horizontalScroll = new ScrollPane(itemContainer);
            horizontalScroll.setFitToHeight(true);
            horizontalScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            horizontalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            horizontalScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            container.getChildren().add(horizontalScroll);
        } else {
            Label body = new Label(content);
            body.setWrapText(true);
            body.getStyleClass().add("section-body");
            container.getChildren().add(body);
        }
        container.getChildren().add(new Separator());
    }

    // [제품 추천 섹션 추가] - 구매 링크가 걸린 제품 이미지 표시
    private static void addProductSection(VBox container, String title, String[] links) {
        Label header = new Label(title);
        header.getStyleClass().add("section-header");
        container.getChildren().add(header);

        HBox productContainer = new HBox(30); // 제품 간 간격 30
        productContainer.setAlignment(Pos.TOP_LEFT);
        productContainer.setPadding(new Insets(15, 0, 15, 10));

        for (String link : links) {
            if (link == null || link.isEmpty()) continue;

            // 1. 링크 주소를 이용해 매핑된 실제 제품 이미지 파일명을 가져옴
            String productImgName = SupplementRecommenderModel.getProductImage(link);

            // 2. 텍스트(displayText)는 null로 전달하여 라벨을 생성하지 않음 (이미지만 표시)
            //    isLink=true 로 설정하여 클릭 이벤트 활성화
            VBox productBox = createIconBox(productImgName, null, true, link);

            productContainer.getChildren().add(productBox);
        }

        container.getChildren().add(productContainer);
    }

    // [공통 아이콘 생성 헬퍼] 이미지와 텍스트를 묶어주는 박스 생성
    // imageIdentifier: 이미지 파일명 또는 영양성분 이름
    // displayText: 이미지 아래 표시할 텍스트 (null이면 표시 안 함)
    // isLink: 클릭 가능 여부 (true면 커서 변경 및 브라우저 오픈)
    private static VBox createIconBox(String imageIdentifier, String displayText, boolean isLink, String url) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(100);

        ImageView imageView = new ImageView();
        try {
            String imageFileName;
            // 입력값이 파일명(.jpg 등 포함)이면 그대로 사용, 아니면 영양성분 이름으로 모델에서 조회
            if (imageIdentifier.contains(".")) {
                imageFileName = imageIdentifier;
            } else {
                imageFileName = new SupplementRecommenderModel().getNutrientImage(imageIdentifier);
            }

            URL imageUrl = RecommendationPopup.class.getResource("/images/" + imageFileName);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                imageView.setImage(image);
            }
        } catch (Exception e) { }

        // 이미지 스타일 (원형 클리핑)
        imageView.setFitWidth(70);
        imageView.setFitHeight(70);
        imageView.setPreserveRatio(true);
        imageView.setClip(new Circle(35, 35, 35));

        box.getChildren().add(imageView);

        // 텍스트 라벨 추가 (displayText가 있을 때만)
        if (displayText != null && !displayText.isEmpty()) {
            Label textLabel = new Label(displayText);
            textLabel.setWrapText(true);
            textLabel.setTextAlignment(TextAlignment.CENTER);
            textLabel.getStyleClass().add("section-body");
            textLabel.setStyle("-fx-font-size: 13px;");

            if (isLink) {
                textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #32baf8; -fx-font-weight: bold;");
            }

            box.getChildren().add(textLabel);
        }

        // 링크 클릭 이벤트 처리
        if (isLink && url != null) {
            box.setStyle("-fx-cursor: hand;"); // 손가락 커서
            box.setOnMouseClicked(e -> {
                try {
                    // 기본 브라우저 열기
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        return box;
    }
}