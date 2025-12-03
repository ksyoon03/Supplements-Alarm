package com.nutrient_reminder;

import com.nutrient_reminder.service.LoginServer;

public class Launcher {
    public static void main(String[] args) {

        // 서버를 별도 스레드에서 실행
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("Launcher 서버 시작");
                LoginServer.main(args); // LoginServer 실행
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 프로그램 종료 시 서버 종료
        serverThread.setDaemon(true);

        // 서버 스레드 시작
        serverThread.start();

        // 기존 GUI 프로그램 실행
        System.out.println("Launcher 클라이언트 시작");
        Main.main(args);
    }
}