package com.nutrient_reminder;

import com.nutrient_reminder.model.Nutrient;
import com.nutrient_reminder.service.NutrientRepository;

import java.util.List;

public class TestSave {
    public static void main(String[] args) {
        System.out.println("========== [테스트 시작] ==========");

        // 1. 저장소(Repository) 객체 생성
        NutrientRepository repository = new NutrientRepository();

        // 2. 테스트용 더미 데이터 생성
        String testName = "테스트비타민";
        String testTime = "오전 10:00";
        // Java 9 이상 사용 시 List.of() 사용 가능. 만약 에러 나면 Arrays.asList()로 변경하세요.
        List<String> testDays = List.of("월", "수", "금");

        Nutrient newNutrient = new Nutrient(testName, testTime, testDays);
        System.out.println("1. 데이터 생성 완료: " + testName);

        // 3. 저장 시도 (save 메서드 호출)
        try {
            repository.save(newNutrient);
            System.out.println("2. 저장 메서드 호출 성공 (nutrient_data.json 파일 생성됨)");
        } catch (Exception e) {
            System.out.println("!!! 저장 중 에러 발생 !!!");
            e.printStackTrace();
            return;
        }

        // 4. 불러오기 시도 (loadAll 메서드 호출)
        System.out.println("3. 파일에서 데이터 다시 읽어오는 중...");
        List<Nutrient> loadedList = repository.loadAll();

        // 5. 검증 (내가 저장한 게 들어있나?)
        boolean isSuccess = false;
        for (Nutrient n : loadedList) {
            System.out.println("   -> 읽어온 데이터: " + n.getName() + " / " + n.getTime() + " / " + n.getDays());

            if (n.getName().equals(testName)) {
                isSuccess = true;
            }
        }

        System.out.println("==================================");
        if (isSuccess) {
            System.out.println("결과: [성공] 데이터가 정상적으로 저장되고 읽혔습니다!");
        } else {
            System.out.println("결과: [실패] 저장된 데이터를 찾을 수 없습니다.");
        }
    }
}