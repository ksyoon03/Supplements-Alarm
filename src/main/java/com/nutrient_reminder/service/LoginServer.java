package com.nutrient_reminder.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class LoginServer {
    // 1. 사용자 정보를 저장할 파일 이름 정의
    private static final String DB_FILE = "users.json";

    // 데이터베이스 역할을 할 Map (메모리 상의 캐시 역할)
    private static Map<String, String> userDatabase = new HashMap<>();

    // JSON 변환기 (전역으로 사용)
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        // 2. 서버 시작 전에 파일에서 사용자 정보 불러오기 (데이터 로딩)
        loadUserDatabase();

        // 8080 포트로 들어오는 요청을 받을 서버 생성
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 각 경로에 맞는 핸들러(요청 처리 담당)를 설정
        server.setExecutor(Executors.newFixedThreadPool(5));
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());

        // 서버 시작
        server.start();
        System.out.println("서버가 8080 포트에서 시작되었습니다.");
        System.out.println("현재 등록된 사용자 수: " + userDatabase.size() + "명");
    }

    // --- [핵심 기능] 파일 저장 및 불러오기 메소드 ---

    // 파일에서 데이터를 읽어와서 userDatabase 맵에 채우는 메소드
    private static void loadUserDatabase() {
        File file = new File(DB_FILE);
        if (file.exists()) {
            try {
                // JSON 파일을 읽어서 Map<String, String> 형태로 변환
                userDatabase = objectMapper.readValue(file, new TypeReference<Map<String, String>>() {});
                System.out.println("기존 사용자 데이터를 불러왔습니다.");
            } catch (IOException e) {
                System.out.println("데이터 로딩 중 오류 발생: " + e.getMessage());
            }
        } else {
            System.out.println("기존 데이터 파일이 없습니다. 새로 시작합니다.");
            // 테스트 계정 하나 추가 (파일이 없을 때만)
            String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
            userDatabase.put("testuser", hashedPassword);
            saveUserDatabase(); // 초기 데이터 저장
        }
    }

    // 현재 userDatabase 맵의 내용을 파일에 저장하는 메소드
    private static synchronized void saveUserDatabase() {
        try {
            // Map 내용을 users.json 파일로 씀 (보기 좋게 들여쓰기 포함)
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(DB_FILE), userDatabase);
            System.out.println("데이터베이스 저장 완료.");
        } catch (IOException e) {
            System.out.println("데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }

    // --- DTO 클래스 ---
    static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    static class RegisterRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    static class ApiResponse {
        private String message;
        public ApiResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    // --- 핸들러 클래스들 ---

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            LoginRequest loginData = objectMapper.readValue(requestBody, LoginRequest.class);

            // 메모리에 로드된 Map에서 확인
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

    static class RegisterHandler implements HttpHandler {
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

            // 3. 메모리에 저장
            userDatabase.put(username, hashedPassword);
            System.out.println("새 사용자 등록: " + username);

            // 4. [중요] 변경된 내용을 파일에도 즉시 저장!
            saveUserDatabase();

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