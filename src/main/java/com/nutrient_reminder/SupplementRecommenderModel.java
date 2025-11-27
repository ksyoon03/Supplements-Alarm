package com.nutrient_reminder;

import java.util.*;

public class SupplementRecommenderModel {

    private static final Map<String, List<String>> YOUNG_YANG = new LinkedHashMap<>();


    private static final Map<String, String[]> YOUNG_YANG_EXPLAIN = new HashMap<>();

    static {
        // --- 1. 24가지 증상 -> 성분 매핑 초기화 ---
        YOUNG_YANG.put("만성 피로", List.of("비타민 B군", "코엔자임 Q10"));
        YOUNG_YANG.put("아침 기상", List.of("비타민 B군", "마그네슘"));
        YOUNG_YANG.put("집중력 저하", List.of("오메가-3", "은행잎 추출물"));
        YOUNG_YANG.put("안구 건조", List.of("오메가-3"));
        YOUNG_YANG.put("눈 피로", List.of("루테인/지아잔틴", "아스타잔틴"));
        YOUNG_YANG.put("잦은 감기", List.of("비타민 D", "아연"));
        YOUNG_YANG.put("구내염/잇몸 염증", List.of("비타민 B군", "비타민 C"));
        YOUNG_YANG.put("소화 불량", List.of("소화 효소", "프로바이오틱스"));
        YOUNG_YANG.put("배변 문제", List.of("프로바이오틱스", "식이섬유"));
        YOUNG_YANG.put("근육 떨림", List.of("마그네슘"));
        YOUNG_YANG.put("손발 저림", List.of("마그네슘", "비타민 B군"));
        YOUNG_YANG.put("관절 불편", List.of("MSM", "글루코사민"));
        YOUNG_YANG.put("근육 뭉침", List.of("마그네슘", "오메가-3"));
        YOUNG_YANG.put("피부 건조", List.of("콜라겐", "히알루론산"));
        YOUNG_YANG.put("피부 트러블", List.of("아연", "오메가-3"));
        YOUNG_YANG.put("모발/손톱 약화", List.of("비오틴", "아연"));
        YOUNG_YANG.put("수면 장애", List.of("마그네슘", "테아닌"));
        YOUNG_YANG.put("스트레스/불안", List.of("테아닌", "마그네슘"));
        YOUNG_YANG.put("수족냉증", List.of("은행잎 추출물", "오메가-3"));
        YOUNG_YANG.put("잦은 음주", List.of("밀크씨슬", "비타민 B군"));
        YOUNG_YANG.put("색소침착/기미", List.of("비타민 C", "비타민 E"));
        YOUNG_YANG.put("골다공증", List.of("칼슘", "마그네슘"));
        YOUNG_YANG.put("체지방/다이어트", List.of("가르시니아", "녹차 추출물"));
        YOUNG_YANG.put("잇몸 질환", List.of("비타민 C", "베타시토스테롤"));

        // --- 2. 영양 성분 상세 정보 초기화 ---
        YOUNG_YANG_EXPLAIN.put("비타민 B군", new String[]{"고함량 B 복합체", "에너지 생성 필수. 아침 식사 후 권장."});
        YOUNG_YANG_EXPLAIN.put("코엔자임 Q10", new String[]{"고순도 코큐텐", "항산화 및 세포 에너지 공급에 도움."});
        YOUNG_YANG_EXPLAIN.put("마그네슘", new String[]{"흡수율 좋은 마그네슘", "신경 안정, 근육 이완에 탁월. 저녁 식후 또는 취침 전."});
        YOUNG_YANG_EXPLAIN.put("오메가-3", new String[]{"고순도 EPA/DHA", "혈행 개선 및 염증 완화. 식후 복용."});
        YOUNG_YANG_EXPLAIN.put("은행잎 추출물", new String[]{"징코 빌로바", "혈액 순환 및 집중력 개선에 도움."});
        YOUNG_YANG_EXPLAIN.put("루테인/지아잔틴", new String[]{"프리미엄 루테인", "눈 건강, 황반 색소 밀도 유지에 도움."});
        YOUNG_YANG_EXPLAIN.put("아스타잔틴", new String[]{"헤마토코쿠스 아스타잔틴", "눈의 피로도 개선에 도움."});
        YOUNG_YANG_EXPLAIN.put("비타민 D", new String[]{"고함량 비타민 D", "면역 및 뼈 건강 필수. 햇빛이 부족할 때 복용."});
        YOUNG_YANG_EXPLAIN.put("아연", new String[]{"킬레이트 아연", "면역 기능 및 피부/모발 건강 지원. 식사 중 또는 식후."});
        YOUNG_YANG_EXPLAIN.put("비타민 C", new String[]{"고함량 비타민 C", "항산화 및 콜라겐 생성 지원. 식후 복용."});
        YOUNG_YANG_EXPLAIN.put("소화 효소", new String[]{"종합 소화 효소", "식후 더부룩함 해소에 도움. 식사 직후."});
        YOUNG_YANG_EXPLAIN.put("프로바이오틱스", new String[]{"프리미엄 유산균", "장 건강 개선. 공복에 섭취 권장."});
        YOUNG_YANG_EXPLAIN.put("식이섬유", new String[]{"차전자피 식이섬유", "배변 활동 원활에 도움. 충분한 물과 함께."});
        YOUNG_YANG_EXPLAIN.put("MSM", new String[]{"MSM/식이유황", "관절 및 연골 조직 건강에 도움."});
        YOUNG_YANG_EXPLAIN.put("글루코사민", new String[]{"글루코사민 복합제", "관절 건강 보조."});
        YOUNG_YANG_EXPLAIN.put("콜라겐", new String[]{"저분자 피쉬 콜라겐", "피부 탄력 및 보습 개선."});
        YOUNG_YANG_EXPLAIN.put("히알루론산", new String[]{"먹는 히알루론산", "피부 및 눈의 수분 유지."});
        YOUNG_YANG_EXPLAIN.put("비오틴", new String[]{"고함량 비오틴", "모발 및 손톱 강화."});
        YOUNG_YANG_EXPLAIN.put("테아닌", new String[]{"L-테아닌", "스트레스 완화 및 수면 질 개선. 저녁 복용."});
        YOUNG_YANG_EXPLAIN.put("밀크씨슬", new String[]{"실리마린 밀크씨슬", "간 건강 보호 및 피로 해소. 아침 또는 점심 식후."});
        YOUNG_YANG_EXPLAIN.put("비타민 E", new String[]{"천연 토코페롤", "항산화 및 피부 건강."});
        YOUNG_YANG_EXPLAIN.put("칼슘", new String[]{"해조 칼슘", "뼈 건강. 비타민 D와 함께 복용 권장."});
        YOUNG_YANG_EXPLAIN.put("가르시니아", new String[]{"HCA 가르시니아", "체지방 감소. 식사 전 복용."});
        YOUNG_YANG_EXPLAIN.put("녹차 추출물", new String[]{"카테킨", "체지방 감소 및 항산화 작용."});
        YOUNG_YANG_EXPLAIN.put("베타시토스테롤", new String[]{"잇몸 전용 영양제", "잇몸 염증 개선에 도움."});
    }

    // 3. 핵심 추천 로직 (Recommendation Logic)
    public List<String> getRecommendations(List<String> selectedSymptoms) {

        // 1. 중복 제거를 위한 LinkedHashSet 선언
        // LinkedHashSet: 중복 자동 제거 및 요소 추가 순서 유지
        Set<String> uniqueNutrients = new LinkedHashSet<>();

        // 2. 증상 목록을 순회하며 모든 추천 성분을 Set에 수집 (중복 제거하기 위해)
        for (String keyword : selectedSymptoms) {
            // 해당 증상에 매핑된 영양 성분 리스트를 Map에서 가져옴.
            List<String> nutrients =  YOUNG_YANG.get(keyword);

            // 가져온 영양 성분 리스트를 순회하며 Set에 하나씩 추가 (이중 for 루프)
            // Set의 add() 기능이 동일한 성분명(예: 비타민 B군)의 중복 추가를 자동으로 막습니다.
            for (String nutrient : nutrients) {
                uniqueNutrients.add(nutrient);
            }
        }

        // 3. 최종 추천 목록을 포맷
        List<String> finalRecommendations = new ArrayList<>();

        // 전체 추천 성분 개수를 포함한 제목 추가
        finalRecommendations.add("===== 종합 추천 영양 성분 (" + uniqueNutrients.size() + "가지) =====");

        // 중복 제거된 성분 목록(Set)을 순회하며 상세 정보를 조회하고 포맷팅
        for (String nutrient : uniqueNutrients) {
            // 성분 이름으로 상세 정보 Map에서 추천 제품 예시 및 참고 사항을 조회
            String[] details = YOUNG_YANG_EXPLAIN.get(nutrient);


                // String.format을 사용하여 최종 출력 형식(성분명, 예시, 참고 사항)으로 문자열을 생성
                String result = String.format(
                        "%s%n   - 추천 제품 예시: %s%n   - 참고 사항: %s%n",
                        nutrient, details[0], details[1]
                );
                finalRecommendations.add(result);

        }

        return finalRecommendations;
    }

    // --------------------------------------------------------------------------------
    // 4. UI 연동 메서드 (UI Integration)
    // --------------------------------------------------------------------------------
    public static List<String> getAllSymptoms() {
        // Map의 KeySet(증상 목록)을 가져와 ArrayList로 변환하여 UI에 전달
        // 이 리스트를 통해 JavaFX 컨트롤러가 체크박스 목록을 동적으로 생성
        return new ArrayList<>(YOUNG_YANG.keySet());
    }
}





