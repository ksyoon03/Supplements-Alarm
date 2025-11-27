package com.nutrient_reminder;

// JavaFX 라이브러리
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
// 입출력 오류 처리를 위한 클래스
import java.io.IOException;

// Application 클래스 상속
// Application 클래스 = 부모 클래스
public class Main extends Application {
    @Override   // 부모 클래스 (Application 클래스)의 메서드를 재정의한다는 것을 명시하는 어노테이션
    public void start(Stage stage) throws IOException {
    // 파일 불러오기 중 오류 발생 시 상위 호출자에게 처리 위임
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/nutrient_reminder/view/login-view.fxml"));

        // login-view.fxml 파일 불러오기
        Scene scene = new Scene(fxmlLoader.load(), 750, 600);
        // 불러온 파일로부터 로드한 UI 요소들을 담는 객체 생성, 가로 450px, 세로 350px
        stage.setTitle("영양제 알리미"); // 프로그램 제목
        stage.setScene(scene); // 로그인 화면 구성
        stage.show();   // 로그인 화면 출력
    }

    public static void main(String[] args) {
        launch(); // 프로그램 실행
    }
}