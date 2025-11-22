package com.nutrient_reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class LoginServer {
    // 가상의 데이터베이스 역할을 할 Map
    private static final Map<String, String> userDatabase = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // 서버 시작 전, 테스트용 사용자 정보를 미리 저장
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        userDatabase.put("testuser", hashedPassword);

        // 8080 포트로 들어오는 요청을 받을 서버 생성
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 각 경로에 맞는 핸들러(요청 처리 담당)를 설정
        server.setExecutor(Executors.newFixedThreadPool(5));
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());

        // 서버 시작
        server.start();
        System.out.println("서버가 8080 포트에서 시작되었습니다.");
    }

    // --- DTO 클래스: JSON 데이터를 담을 자바 객체들 ---

    static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    // --- 추가된 부분 ---
    static class RegisterRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    // --- 추가된 부분 ---
    // 공통 응답용 DTO
    static class ApiResponse {
        private String message;
        public ApiResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }


    // --- 핸들러 클래스들 ---

    static class LoginHandler implements HttpHandler {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            LoginRequest loginData = objectMapper.readValue(requestBody, LoginRequest.class);
            String storedHashedPassword = userDatabase.get(loginData.getUsername());

            if (storedHashedPassword != null && BCrypt.checkpw(loginData.getPassword(), storedHashedPassword)) {
                System.out.println(loginData.getUsername() + " 님 로그인 성공");
                sendJsonResponse(exchange, 200, new ApiResponse("로그인 성공!"));
            } else {
                System.out.println(loginData.getUsername() + " 님 로그인 실패");
                sendJsonResponse(exchange, 401, new ApiResponse("아이디 또는 비밀번호가 잘못되었습니다."));
            }
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, jsonBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonBytes);
            os.close();
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    // --- 추가된 부분 ---
    // 회원가입 요청을 처리하는 핸들러
    static class RegisterHandler implements HttpHandler {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            RegisterRequest registerData = objectMapper.readValue(requestBody, RegisterRequest.class);
            String username = registerData.getUsername();
            String password = registerData.getPassword();

            if (userDatabase.containsKey(username)) {
                System.out.println("회원가입 실패: 아이디 중복 (" + username + ")");
                sendJsonResponse(exchange, 409, new ApiResponse("이미 사용 중인 아이디입니다."));
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            userDatabase.put(username, hashedPassword);
            System.out.println("새 사용자 등록: " + username);

            sendJsonResponse(exchange, 201, new ApiResponse("회원가입이 완료되었습니다."));
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, jsonBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonBytes);
            os.close();
        }
        private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}