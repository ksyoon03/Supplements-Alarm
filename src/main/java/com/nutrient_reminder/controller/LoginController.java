package com.nutrient_reminder.controller;

// JavaFX 활용을 위한 요소들
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

// 웹 서버 통신을 위한 클래스들
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//++ 회원가입 하이퍼링크 추가
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;
import java.io.IOException;


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
        // String.format() : 문자열의 형식을 미리 정해놓고, 원하는 값을 끼워 넣어 새로운 문자열을 만드는 기능


        HttpRequest request = HttpRequest.newBuilder()
        // Http 요청 사항을 위한 빌더 객체 생성
                                .uri(URI.create("http://localhost:8080/api/login"))
                                // 요청을 보낼 주소 설정 ("/api/login")
                                .header("Content-Type", "application/json")
                                // 요청의 메타데이터(부가 정보)인 header 추가
                                // Content-Type : 보내는 메타데이터의 종류는
                                // application/json : json 형식이다
                                .POST(HttpRequest.BodyPublishers.ofString(json))
                                // 요청 방식을 Post로 지정, 요청에 담아 보낼 실제 데이터(Body) 설정
                                .build();
                                // 위의 정보들을 종합하여 변경 불가능한 최종 HttpRequest 객체 생성

        try{
            // 요청을 보내고 응답을 받음
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // 1. client.send(...) : HttpClient 객체인 client가 .send() 메소드를 호출해서 요청을 전송
            // 2. request : HttpRequest 객체인 request의 요청 객체
            // 3. HttpResponse.BodyHandlers.ofString() : 서버로부터 받은 JSON 문자열을 String 타입으로 변환
            // 4. HttpResponse<String> response : 1번을 통해 서버로부터 받은 응답을 HttpResponse 객체로 반환

            // 서버의 응답 코드 확인
            if(response.statusCode() == 200){   // 200 = 성공을 의미, 401 = 인증 실패, 500 = 서버 오류
                System.out.println("로그인 성공");

                // 영양제 추천 페이지로 이동
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nutrient_reminder/view/nutrient-check.fxml"));
                Parent root = loader.load();

                NutrientCheckController controller = loader.getController();
                controller.setUsername(username);

                Stage stage = (Stage) idField.getScene().getWindow();
                stage.setScene(new Scene(root, 750, 600));
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

    //++ 회원가입 하이퍼링크 추가
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

        // 화면 표시
        stage.show();
    }

}
