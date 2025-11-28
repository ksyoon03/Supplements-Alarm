package com.nutrient_reminder.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public class SignupController {


    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Button signupButton;
    @FXML private Label hintLabel;


    //HTTP 통신 클라이언트 설정
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();


    //상수 정의
    private static final String BASE_URL = "http://localhost:8080";   // 서버 주소
    private static final Pattern USERNAME_RULE = Pattern.compile("^[a-zA-Z0-9]{4,20}$"); // 아이디 정규식
    private static final int PW_MIN = 8;   // 비밀번호 최소 길이
    private static final int PW_MAX = 32;  // 비밀번호 최대 길이


    // 초기화 (화면 로딩 시 1회 실행)
    @FXML
    private void initialize() {
        // Enter 키를 눌러도 회원가입 버튼과 동일하게 작동하도록 설정
        usernameField.setOnAction(e -> signupButton.fire());
        passwordField.setOnAction(e -> signupButton.fire());
        confirmField.setOnAction(e -> signupButton.fire());
    }


    // 회원가입 버튼 클릭 시 실행
    @FXML
    private void signupAction() {
        // 입력값 가져오기 및 공백 제거
        String username = safe(usernameField.getText()).trim();
        String password = safe(passwordField.getText());
        String confirm  = safe(confirmField.getText());

        // 입력값 유효성 검사
        String err = validate(username, password, confirm);
        if (err != null) {
            showAlert(Alert.AlertType.WARNING, "입력 확인", err);
            return;
        }

        // 서버로 보낼 JSON 데이터 생성
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                escapeJson(username), escapeJson(password));

        // 회원가입 API 요청 생성
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/register"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // 4버튼 비활성화 후 비동기 요청 전송
        setBusy(true);
        http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .whenComplete((resp, throwable) -> {
                    Platform.runLater(() -> setBusy(false));


                    if (throwable != null) {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.ERROR, "네트워크 오류", throwable.getMessage()));
                        return;
                    }

                    int status = resp.statusCode();
                    String body = resp.body();
                    String msg  = extractMessage(body);

                    if (status == 201) { // 회원가입 성공
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "회원가입 완료",
                                    msg.isEmpty() ? "회원가입이 완료되었습니다." : msg);
                            usernameField.clear();
                            passwordField.clear();
                            confirmField.clear();
                            navigateToLogin();
                        });
                    } else if (status == 409) {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.WARNING, "아이디 중복",
                                        msg.isEmpty() ? "이미 사용 중인 아이디입니다." : msg));
                    } else if (status == 400) {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.WARNING, "요청 형식 오류",
                                        msg.isEmpty() ? "입력값을 확인해주세요." : msg));
                    } else {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.ERROR, "서버 오류",
                                        "요청 실패 (" + status + ")\n" + (msg.isEmpty() ? body : msg)));
                    }
                });
    }

    // ++ 로그인 화면으로 이동
    private void navigateToLogin() {

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("로그인");
            stage.show(); // 새 화면 표시
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "화면 전환 오류", "로그인 화면으로 이동할 수 없습니다.");
        }
    }

    // 전송 중 버튼 비활성화
    private void setBusy(boolean busy) {
        signupButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        confirmField.setDisable(busy);
    }


    // 입력값 유효성 검증
    private String validate(String username, String password, String confirm) {
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty())
            return "아이디/비밀번호를 모두 입력해주세요.";
        if (!USERNAME_RULE.matcher(username).matches())
            return "아이디는 영문/숫자 4~20자만 가능합니다.";
        if (password.length() < PW_MIN || password.length() > PW_MAX)
            return "비밀번호는 8~32자여야 합니다.";
        if (!password.equals(confirm))
            return "비밀번호와 확인이 일치하지 않습니다.";
        return null;
    }


    //서버 응답(JSON)에서 메시지 추출
    private String extractMessage(String body) {
        if (body == null) return "";
        int i = body.indexOf("\"message\"");
        if (i < 0) return body;
        int colon = body.indexOf(':', i);
        int firstQuote = body.indexOf('"', colon + 1);
        int secondQuote = body.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) return body;
        return body.substring(firstQuote + 1, secondQuote);
    }


    //JSON 문자열 이스케이프 처리
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }


    //null 방지용 안전 처리
    private String safe(String s) {
        return s == null ? "" : s;
    }


    // 공통 알림창 표시
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("알림");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    //로그인 화면으로 이동
    @FXML
    private void goToLogin(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/nutrient_reminder/view/login-view.fxml"));

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("로그인");
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
