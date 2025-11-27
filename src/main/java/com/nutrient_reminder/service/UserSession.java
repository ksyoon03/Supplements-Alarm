package com.nutrient_reminder.service;

public class UserSession {
    // 프로그램이 켜져 있는 동안 로그인 정보를 저장할 공간 (전광판)
    private static String userId;

    // 아이디 저장하기
    public static void setUserId(String id) {
        userId = id;
    }

    // 아이디 꺼내오기
    public static String getUserId() {
        return userId;
    }

    // 로그아웃 (초기화)
    public static void clear() {
        userId = null;
    }
}