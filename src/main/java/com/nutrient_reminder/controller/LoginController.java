package com.nutrient_reminder.controller;

import com.nutrient_reminder.service.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField idField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void loginButtonAction(ActionEvent event){
        String username = idField.getText();
        String password = passwordField.getText();

        // 입력값 검증
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "입력 확인", "아이디와 비밀번호를 모두 입력해주세요.");
            return;
        }

        // HttpClient 클래스 객체 생성
        HttpClient client = HttpClient.newHttpClient();

        // 서버로 보낼 JSON 데이터 생성
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try{
            // 요청을 보내고 응답을 받음
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 서버의 응답 코드 확인
            if(response.statusCode() == 200){
                System.out.println("로그인 성공");

                //UserSession에 아이디 저장!
                UserSession.setUserId(username);

                // 이동할 화면을 'main.fxml'로 변경
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/main.fxml"));
                Parent root = loader.load();

                // 화면 전환 및 창 제목 설정
                Stage stage = (Stage) idField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("영양제 알리미");

                // 수정: 화면 전환 후 최대화 다시 적용
                stage.setMaximized(true);
                stage.show();


            }
            else{
                System.out.println("로그인 실패" + response.body());
                showAlert(Alert.AlertType.ERROR, "로그인 실패", "아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "서버 연결 오류", "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.");
        }
        catch(InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "요청 오류", "요청이 중단되었습니다.");
        }


        System.out.println("로그인 진행");
        System.out.println("아이디: " + username);
        System.out.println("비밀번호: " + password);
    }

    // 알림창 표시 메서드 추가
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("알림");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //++ 회원가입 하이퍼링크 추가 (>>> main 버전의 깔끔한 전환 로직 사용)
    @FXML
    private void goToSignup(ActionEvent e) throws IOException {
        // signup.fxml 파일을 불러와 새로운 화면 구성
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(
                        getClass().getResource("/com/nutrient_reminder/view/signup.fxml")
                )
        );

        // 현재 창(Stage)을 가져와 화면 전환
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        // 새 Scene 설정 및 타이틀 변경
        stage.setScene(new Scene(root));
        stage.setTitle("회원가입");

        // 수정: 화면 전환 후 최대화 다시 적용
        stage.setMaximized(true);

        // 화면 표시
        stage.show();
    }

}