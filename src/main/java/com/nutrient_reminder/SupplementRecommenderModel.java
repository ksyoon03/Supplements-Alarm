package com.nutrient_reminder;

import java.util.*;

public class SupplementRecommenderModel {

    // 1. 증상 -> 추천 영양성분 매핑
    private static final Map<String, List<String>> YOUNG_YANG = new LinkedHashMap<>();

    // 2. 영양성분 상세 설명
    private static final Map<String, String[]> YOUNG_YANG_EXPLAIN = new HashMap<>();

    // 3. 데이터 세분화 (저장소를 3개로 완벽하게 분리함)
    private static final Map<String, String> GOOD_COMBINATIONS = new HashMap<>(); // 같이 먹으면 좋은 성분
    private static final Map<String, String> BAD_COMBINATIONS = new HashMap<>();  // 같이 먹으면 안 좋은 성분
    private static final Map<String, String> INTAKE_TIPS = new HashMap<>();       // 섭취 시간 팁

    // 4. 영양성분별 구매 링크
    private static final Map<String, String[]> NUTRIENT_LINKS = new HashMap<>();

    // 5. 영양성분 이미지 파일 경로 매핑
    private static final Map<String, String> NUTRIENT_IMAGES = new HashMap<>();

    // 6. 실제 제품 이미지 매핑 (링크 -> 제품 사진 파일명)
    private static final Map<String, String> PRODUCT_IMAGES = new HashMap<>();

    static {
        // ==========================================
        // 1. 증상 -> 영양성분 데이터
        // ==========================================
        YOUNG_YANG.put("만성 피로", List.of("비타민 B군", "비타민 C"));
        YOUNG_YANG.put("스트레스 피로", List.of("홍경천"));
        YOUNG_YANG.put("신경 과민/긴장", List.of("테아닌", "마그네슘"));
        YOUNG_YANG.put("수면 부족 (불면)", List.of("테아닌", "마그네슘"));
        YOUNG_YANG.put("기억력 감퇴", List.of("포스파티딜세린"));
        YOUNG_YANG.put("눈 침침함 (노안)", List.of("루테인", "비타민 A"));
        YOUNG_YANG.put("눈 건조/피로", List.of("오메가3", "비타민 A"));
        YOUNG_YANG.put("야맹증", List.of("비타민 A"));
        YOUNG_YANG.put("구내염/입병", List.of("비타민 B2", "비타민 C"));
        YOUNG_YANG.put("속당김 (피부 건조)", List.of("히알루론산", "비타민 A"));
        YOUNG_YANG.put("피부 노화 (주름)", List.of("비타민 C", "비타민 E"));
        YOUNG_YANG.put("피부 트러블 (여드름)", List.of("아연", "비타민 A"));
        YOUNG_YANG.put("멍이 잘 듦", List.of("비타민 C"));
        YOUNG_YANG.put("탈모/모발 약화", List.of("비오틴", "아연"));
        YOUNG_YANG.put("손톱 갈라짐", List.of("비오틴"));
        YOUNG_YANG.put("배변 활동 (변비)", List.of("유산균"));
        YOUNG_YANG.put("장 건강 (가스)", List.of("유산균"));
        YOUNG_YANG.put("식욕 부진", List.of("아연"));
        YOUNG_YANG.put("잦은 음주 (간)", List.of("밀크씨슬"));
        YOUNG_YANG.put("탄수화물 과다 (살)", List.of("가르시니아", "비타민 B1", "판토텐산"));
        YOUNG_YANG.put("혈중 중성지방", List.of("오메가3"));
        YOUNG_YANG.put("혈행 개선 (순환)", List.of("오메가3", "달맞이꽃종자유"));
        YOUNG_YANG.put("관절 통증", List.of("MSM"));
        YOUNG_YANG.put("골다공증", List.of("비타민 D", "칼슘", "마그네슘"));
        YOUNG_YANG.put("근육 떨림/경련", List.of("마그네슘"));
        YOUNG_YANG.put("빈혈/어지러움", List.of("철분", "엽산"));
        YOUNG_YANG.put("면역력 저하", List.of("아연", "비타민 C", "비타민 D", "유산균"));
        YOUNG_YANG.put("월경 전 증후군", List.of("달맞이꽃종자유"));

        // ==========================================
        // 2. 상세 설명
        // ==========================================
        YOUNG_YANG_EXPLAIN.put("비타민 A", new String[]{"비타민 A", "①어두운 곳에서 시각 적응을 위해 필요\n②피부와 점막을 형성하고 기능을 유지하는데 필요\n③상피세포의 성장과 발달에 필요"});
        YOUNG_YANG_EXPLAIN.put("비타민 B1", new String[]{"비타민 B1", "①탄수화물과 에너지 대사에 필요"});
        YOUNG_YANG_EXPLAIN.put("비타민 B2", new String[]{"비타민 B2", "①체내 에너지 생성에 필요"});
        YOUNG_YANG_EXPLAIN.put("비타민 C", new String[]{"비타민 C", "①결합조직 형성과 기능유지에 필요\n②철의 흡수에 필요\n③유해산소로부터 세포를 보호하는데 필요"});
        YOUNG_YANG_EXPLAIN.put("비타민 D", new String[]{"비타민 D", "①칼슘과 인이 흡수되고 이용되는데 필요\n②뼈의 형성과 유지에 필요\n③골다공증 발생 위험 감소에 도움을 줌"});
        YOUNG_YANG_EXPLAIN.put("비타민 E", new String[]{"비타민 E", "①유해산소로부터 세포를 보호하는데 필요"});
        YOUNG_YANG_EXPLAIN.put("비타민 B군", new String[]{"비타민 B군", "①탄수화물과 에너지 대사에 필요(B1)\n②체내 에너지 생성에 필요(B2)\n③단백질 및 아미노산 이용에 필요(B6)\n④혈액의 호모시스테인 수준 정상 유지(B6)"});
        YOUNG_YANG_EXPLAIN.put("판토텐산", new String[]{"판토텐산", "①지방, 탄수화물, 단백질 대사와 에너지 생성에 필요"});
        YOUNG_YANG_EXPLAIN.put("엽산", new String[]{"엽산", "①세포와 혈액생성에 필요\n②태아 신경관의 정상 발달에 필요"});
        YOUNG_YANG_EXPLAIN.put("칼슘", new String[]{"칼슘", "①뼈와 치아 형성에 필요\n②신경과 근육 기능 유지에 필요\n③골다공증 발생 위험 감소에 도움을 줌"});
        YOUNG_YANG_EXPLAIN.put("마그네슘", new String[]{"마그네슘", "①에너지 이용에 필요\n②신경과 근육 기능 유지에 필요"});
        YOUNG_YANG_EXPLAIN.put("철분", new String[]{"철분", "①체내 산소운반과 혈액생성에 필요\n②에너지 생성에 필요"});
        YOUNG_YANG_EXPLAIN.put("아연", new String[]{"아연", "①정상적인 면역기능에 필요\n②정상적인 세포분열에 필요"});
        YOUNG_YANG_EXPLAIN.put("루테인", new String[]{"루테인", "①노화로 인해 감소될 수 있는 황반색소밀도를 유지하여 눈 건강에 도움을 줌"});
        YOUNG_YANG_EXPLAIN.put("오메가3", new String[]{"오메가3", "①혈중 중성지질 개선·혈행 개선\n②건조한 눈을 개선하여 눈 건강에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("유산균", new String[]{"유산균", "①유산균 증식 및 유해균 억제에 도움을 줄 수 있음\n②배변활동 원활에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("밀크씨슬", new String[]{"밀크씨슬", "①간 건강에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("MSM", new String[]{"MSM", "①관절 및 연골건강에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("홍경천", new String[]{"홍경천", "①스트레스로 인한 피로개선에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("테아닌", new String[]{"테아닌", "①스트레스로 인한 긴장완화에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("포스파티딜세린", new String[]{"포스파티딜세린", "①노화로 인해 저하된 인지력 개선\n②피부건강 유지에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("가르시니아", new String[]{"가르시니아", "①탄수화물이 지방으로 합성되는 것을 억제하여 체지방 감소에 도움을 줌"});
        YOUNG_YANG_EXPLAIN.put("달맞이꽃종자유", new String[]{"달맞이꽃종자유", "①식후 혈당상승 억제\n②월경 전 불편함 개선에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("히알루론산", new String[]{"히알루론산", "①피부보습에 도움을 줄 수 있음"});
        YOUNG_YANG_EXPLAIN.put("비오틴", new String[]{"비오틴", "①지방, 탄수화물, 단백질 대사와 에너지 생성에 필요"});

        // ====================================================================
        // 3. 데이터 세분화 적용 (분류 주석 제거됨)
        // (addNutrientInfo 메서드가 각각의 맵에 데이터를 나눠서 담습니다)
        // ====================================================================
        addNutrientInfo("철분",
                "비타민 C",
                "칼슘, 마그네슘, 아연",
                "공복 섭취 권장");

        addNutrientInfo("칼슘",
                "비타민 D, 마그네슘",
                "철분,",
                "식사 직후 권장");

        addNutrientInfo("마그네슘",
                "비타민 B6, 칼슘",
                "철분",
                "저녁 식후 또는 취침 전");

        addNutrientInfo("아연",
                "비타민 A",
                "철분, 칼슘",
                "식사 중/식후");

        addNutrientInfo("비타민 D",
                "칼슘, 마그네슘, 오메가3",
                "특별한 상극 없음",
                "점심/저녁 식후");

        addNutrientInfo("비타민 E",
                "오메가3, 비타민 C",
                "고용량 비타민 K",
                "식후 섭취");

        addNutrientInfo("비타민 A",
                "아연",
                "루테인",
                "식후 섭취");

        addNutrientInfo("오메가3",
                "비타민 E, 비타민 D, 루테인",
                "키토산",
                "식후 섭취");

        addNutrientInfo("루테인",
                "오메가3",
                "비타민 A",
                "아침 식후 권장");

        addNutrientInfo("달맞이꽃종자유",
                "아연, 비타민 B6",
                "항응고제 복용 시 주의",
                "식후 섭취");

        // 비타민 B군은 종류가 많지만 내용은 동일하게 적용
        String bGroupGood = "서로 함께 섭취 (B군 복합제), 아연";
        String bGroupBad = "특별한 상극 없음";
        String bGroupTime = "아침/점심 식후 동시 섭취 추천";

        addNutrientInfo("비타민 B군", bGroupGood, bGroupBad, bGroupTime);
        addNutrientInfo("비타민 B1", bGroupGood, bGroupBad, bGroupTime);
        addNutrientInfo("비타민 B2", bGroupGood, bGroupBad, bGroupTime);
        addNutrientInfo("판토텐산", bGroupGood, bGroupBad, bGroupTime);
        addNutrientInfo("엽산", bGroupGood, bGroupBad, bGroupTime);
        addNutrientInfo("비오틴", bGroupGood, bGroupBad, bGroupTime);

        addNutrientInfo("비타민 C",
                "철분, MSM, 비타민 E",
                "유산균",
                "식중/식후");

        addNutrientInfo("유산균",
                " ",
                "비타민C, 항생제, 프로폴리스",
                "아침 공복 또는 식전");

        addNutrientInfo("밀크씨슬",
                "비타민 B군",
                "특별한 상극 없음",
                "저녁 식후");

        addNutrientInfo("MSM",
                "비타민 C",
                "특별한 상극 없음",
                "식후");

        addNutrientInfo("테아닌",
                "마그네슘",
                "카페인",
                "스트레스 받을 때 또는 자기 전");

        addNutrientInfo("가르시니아",
                " ",
                "당뇨약 복용 시 주의",
                "탄수화물 식사 전");

        addNutrientInfo("홍경천",
                "테아닌, 마그네슘",
                "특별한 상극 없음",
                "오전/낮");

        addNutrientInfo("포스파티딜세린",
                "오메가3, 비타민 E",
                "특별한 상극 없음",
                "식후 섭취");

        addNutrientInfo("히알루론산",
                "콜라겐, 비타민 C",
                "특별한 상극 없음",
                "식후 섭취");


        // ==========================================
        // 5. 영양성분별 구매 링크 (사진 1, 2, 3 순서)
        // ==========================================
        NUTRIENT_LINKS.put("가르시니아", new String[]{
                "https://www.coupang.com/vp/products/6697513406?itemId=21224855184&vendorItemId=73464152763&sourceType=srp_product_ads&clickEventId=90f1a150-ccef-11f0-9992-2b04fdd17277&korePlacement=15&koreSubPlacement=1&clickEventId=90f1a150-ccef-11f0-9992-2b04fdd17277&korePlacement=15&koreSubPlacement=1&traceId=mijxlrju",
                "https://www.coupang.com/vp/products/6807918291?itemId=24020097347&vendorItemId=3028473540&q=가르시니아&searchId=50e8bf993278528&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijxoabt",
                "https://www.coupang.com/vp/products/6681304496?itemId=15407541825&vendorItemId=70056438557&sourceType=srp_product_ads&clickEventId=90f1c860-ccef-11f0-953b-404927f5581d&korePlacement=15&koreSubPlacement=5&clickEventId=90f1c860-ccef-11f0-953b-404927f5581d&korePlacement=15&koreSubPlacement=5&traceId=mijxol0p"
        });
        NUTRIENT_LINKS.put("달맞이꽃종자유", new String[]{
                "https://www.coupang.com/vp/products/6789736242?itemId=18437189193&vendorItemId=3000040918&pickType=COU_PICK&q=달맞이꽃종자유&searchId=ce7d708911707448&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijxqx41",
                "https://www.coupang.com/vp/products/7338313739?itemId=25780653324&vendorItemId=92878998817&q=달맞이꽃종자유&searchId=ce7d708911707448&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijxs56p",
                "https://www.coupang.com/vp/products/7788207539?itemId=21064308808&vendorItemId=3000279175&q=달맞이꽃종자유&searchId=ce7d708911707448&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijxsd8h"
        });
        NUTRIENT_LINKS.put("루테인", new String[]{
                "https://www.coupang.com/vp/products/8936251513?itemId=26127558615&vendorItemId=93687865713&q=루테인&searchId=29d8d55b489507&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijxwitd",
                "https://www.coupang.com/vp/products/171811?itemId=18204786075&vendorItemId=86203373195&q=루테인&searchId=29d8d55b489507&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijxx6qh",
                "https://www.coupang.com/vp/products/8181315530?itemId=23541389413&vendorItemId=91690674857&sourceType=srp_product_ads&clickEventId=b2f121d0-ccf0-11f0-9c80-56675a98e1ae&korePlacement=15&koreSubPlacement=11&clickEventId=b2f121d0-ccf0-11f0-9c80-56675a98e1ae&korePlacement=15&koreSubPlacement=11&traceId=mijxxktu"
        });
        NUTRIENT_LINKS.put("마그네슘", new String[]{
                "https://www.coupang.com/vp/products/7792384678?itemId=21084993184&vendorItemId=79415792989&q=마그네슘&searchId=3b23800f3708400&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijxyf82",
                "https://www.coupang.com/vp/products/6527425583?itemId=14485037757&vendorItemId=76228037698&q=마그네슘&searchId=3b23800f3708400&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijxz0o1",
                "https://www.coupang.com/vp/products/1366689007?itemId=23951551099&vendorItemId=94119849475&q=마그네슘&searchId=3b23800f3708400&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijxzfo1"
        });
        NUTRIENT_LINKS.put("밀크씨슬", new String[]{
                "https://www.coupang.com/vp/products/7156986260?itemId=13254294391&vendorItemId=3180741512&q=밀크씨슬&searchId=650b2c673332192&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijy1b4y",
                "https://www.coupang.com/vp/products/6703806340?itemId=18989222194&vendorItemId=88618091836&q=밀크씨슬&searchId=650b2c673332192&sourceType=search&itemsCount=36&searchRank=16&rank=16&traceId=mijy1yd5",
                "https://www.coupang.com/vp/products/7346382208?itemId=15003949840&vendorItemId=92127531279&sourceType=srp_product_ads&clickEventId=44eeca10-ccf1-11f0-894a-639ccbe6e697&korePlacement=15&koreSubPlacement=9&clickEventId=44eeca10-ccf1-11f0-894a-639ccbe6e697&korePlacement=15&koreSubPlacement=9&traceId=mijy3mpt"
        });
        NUTRIENT_LINKS.put("비오틴", new String[]{
                "https://www.coupang.com/vp/products/1955584555?itemId=23409820872&vendorItemId=72495291625&q=비오틴&searchId=02d4497e523860&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijy4eoi",
                "https://www.coupang.com/vp/products/7141471981?itemId=17930490564&vendorItemId=85092793761&q=비오틴&searchId=02d4497e523860&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijy5a8h",
                "https://www.coupang.com/vp/products/220719097?itemId=688523660&vendorItemId=4765824817&sourceType=SDW_TOP_SELLING_WIDGET_V2&searchId=02d4497e523860&q=비오틴"
        });
        NUTRIENT_LINKS.put("비타민 A", new String[]{
                "https://www.coupang.com/vp/products/2878672?itemId=13411639&vendorItemId=3272326293&q=비타민+A&searchId=f6807d682740812&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijy6x7e",
                "https://www.coupang.com/vp/products/216731272?itemId=666484606&vendorItemId=4722784812&q=비타민+A&searchId=f6807d682740812&sourceType=search&itemsCount=36&searchRank=19&rank=19&traceId=mijy7q7m",
                "https://www.coupang.com/vp/products/6739267330?itemId=15726380856&vendorItemId=3253073418&q=비타민+A&searchId=f6807d682740812&sourceType=search&itemsCount=36&searchRank=30&rank=30&traceId=mijy94n5"
        });
        NUTRIENT_LINKS.put("비타민 B1", new String[]{
                "https://www.coupang.com/vp/products/8289756716?itemId=12115396226&vendorItemId=71973191016&sourceType=srp_product_ads&clickEventId=2fec1b80-ccf2-11f0-82d5-7f49eb08a1d2&korePlacement=15&koreSubPlacement=1&clickEventId=2fec1b80-ccf2-11f0-82d5-7f49eb08a1d2&korePlacement=15&koreSubPlacement=1&traceId=mijy9qbt",
                "https://www.coupang.com/vp/products/28024?itemId=68076&vendorItemId=3086634180&q=비타민+B1&searchId=2307dbf13367743&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyao9m",
                "https://www.coupang.com/vp/products/3248548?itemId=2358605063&vendorItemId=3273080985&q=비타민+B1&searchId=2307dbf13367743&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijybbpd"
        });
        NUTRIENT_LINKS.put("비타민 B2", new String[]{
                "https://www.coupang.com/vp/products/216731274?itemId=666484610&vendorItemId=4722784826&q=비타민+B2&searchId=d4b13d2e2761049&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyc07m",
                "https://www.coupang.com/vp/products/8075002605?itemId=24174618743&vendorItemId=92932633520&q=비타민+B2&searchId=d4b13d2e2761049&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijycq7u",
                "https://www.coupang.com/vp/products/28026?itemId=68078&vendorItemId=88071243392&q=비타민+B2&searchId=d4b13d2e2761049&sourceType=search&itemsCount=36&searchRank=5&rank=5&traceId=mijydaz6"
        });
        NUTRIENT_LINKS.put("비타민 B군", new String[]{
                "https://www.coupang.com/vp/products/8289158533?itemId=18169956995&vendorItemId=84910700916&q=비타민+B군&searchId=a647a63c547010&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijydzca",
                "https://www.coupang.com/vp/products/8136129840?itemId=9354802&vendorItemId=3180840912&q=비타민+B군&searchId=a647a63c547010&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyeqf5",
                "https://www.coupang.com/vp/products/7449869595?itemId=19391986964&vendorItemId=74820920119&q=비타민+B군&searchId=a647a63c547010&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyf70a"
        });
        NUTRIENT_LINKS.put("비타민 C", new String[]{
                "https://www.coupang.com/vp/products/9192490440?itemId=16777795279&vendorItemId=3000235591&pickType=COU_PICK&q=비타민+C&searchId=e52bc1f94000926&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyg15e",
                "https://www.coupang.com/vp/products/6903873562?itemId=1088946&vendorItemId=3001047907&q=비타민+C&searchId=e52bc1f94000926&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijygc0q",
                "https://www.coupang.com/vp/products/9095157389?itemId=26981112155&vendorItemId=88472838413&sourceType=srp_product_ads&clickEventId=dcda95b0-ccf2-11f0-9f5c-b7c784183813&korePlacement=15&koreSubPlacement=5&clickEventId=dcda95b0-ccf2-11f0-9f5c-b7c784183813&korePlacement=15&koreSubPlacement=5&traceId=mijyhdfu"
        });
        NUTRIENT_LINKS.put("비타민 D", new String[]{
                "https://www.coupang.com/vp/products/21912752?itemId=85422647&vendorItemId=3145132773&q=비타민+D&searchId=e2bf0cb72904901&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyihoa",
                "https://www.coupang.com/vp/products/1780508524?itemId=3031702911&vendorItemId=4722784677&q=비타민+D&searchId=e2bf0cb72904901&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyj8de",
                "https://www.coupang.com/vp/products/220719096?itemId=688523657&vendorItemId=4765824832&q=비타민+D&searchId=e2bf0cb72904901&sourceType=search&itemsCount=36&searchRank=14&rank=14&traceId=mijyjm4p"
        });
        NUTRIENT_LINKS.put("비타민 E", new String[]{
                "https://www.coupang.com/vp/products/1581929897?itemId=2704389446&vendorItemId=70694701289&q=비타민+E&searchId=70c58c2a3994170&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijykeje",
                "https://www.coupang.com/vp/products/2878792?itemId=13412176&vendorItemId=3272319048&q=비타민+E&searchId=70c58c2a3994170&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyl5oy",
                "https://www.coupang.com/vp/products/587394?itemId=70353&vendorItemId=3086364645&q=비타민+E&searchId=70c58c2a3994170&sourceType=search&itemsCount=36&searchRank=8&rank=8&traceId=mijymn8q"
        });
        NUTRIENT_LINKS.put("아연", new String[]{
                "https://www.coupang.com/vp/products/6215299433?itemId=1184751&vendorItemId=3001141888&q=아연&searchId=4f82d07711843317&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijynxu2",
                "https://www.coupang.com/vp/products/2878729?itemId=13411906&vendorItemId=3272333667&q=아연&searchId=4f82d07711843317&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyol02",
                "https://www.coupang.com/vp/products/7924178640?itemId=22812356618&vendorItemId=89847224757&q=아연&searchId=4f82d07711843317&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyozg2"
        });
        NUTRIENT_LINKS.put("엽산", new String[]{
                "https://www.coupang.com/vp/products/29304?itemId=70356&vendorItemId=3086361478&q=엽산&searchId=191a7dbd592515&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijypn6a",
                "https://www.coupang.com/vp/products/7823882369?itemId=21258939802&vendorItemId=3001142329&pickType=COU_PICK&q=엽산&searchId=191a7dbd592515&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyqe0y",
                "https://www.coupang.com/vp/products/2878679?itemId=13411667&vendorItemId=3272327980&q=엽산&searchId=191a7dbd592515&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijyqr1e"
        });
        NUTRIENT_LINKS.put("오메가3", new String[]{
                "https://www.coupang.com/vp/products/6854281548?itemId=24023322885&vendorItemId=4765824687&q=오메가3&searchId=ca212620599945&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijyrgxe",
                "https://www.coupang.com/vp/products/7821106059?itemId=21244482032&vendorItemId=3000085957&q=오메가3&searchId=ca212620599945&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyscsq",
                "https://www.coupang.com/vp/products/7559679373?itemId=10076200032&vendorItemId=70393847077&q=오메가3&searchId=aae86a41617377&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyt9my"
        });
        NUTRIENT_LINKS.put("철분", new String[]{
                "https://www.coupang.com/vp/products/1181654?itemId=5108920&vendorItemId=3006347663&pickType=COU_PICK&q=철분&searchId=1a6dc623620472&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijytvsx",
                "https://www.coupang.com/vp/products/3239450?itemId=14966971&vendorItemId=3273248238&q=철분&searchId=1a6dc623620472&sourceType=search&itemsCount=36&searchRank=12&rank=12&traceId=mijyuhte",
                "https://www.coupang.com/vp/products/2557580?itemId=24043582673&vendorItemId=88071309863&q=철분&searchId=1a6dc623620472&sourceType=search&itemsCount=36&searchRank=23&rank=23&traceId=mijyx08i"
        });
        NUTRIENT_LINKS.put("칼슘", new String[]{
                "https://www.coupang.com/vp/products/8289193023?itemId=70377&vendorItemId=3086591291&q=칼슘&searchId=550aad1a2376275&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyxn2r",
                "https://www.coupang.com/vp/products/8289178801?itemId=9357481&vendorItemId=3267477799&q=칼슘&searchId=550aad1a2376275&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyxwsa",
                "https://www.coupang.com/vp/products/6509173131?itemId=14369677042&vendorItemId=81614046301&q=칼슘&searchId=550aad1a2376275&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyy8yh"
        });
        NUTRIENT_LINKS.put("테아닌", new String[]{
                "https://www.coupang.com/vp/products/8289046224?itemId=17187727471&vendorItemId=4722785945&q=테아닌&searchId=72c2b80111887548&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyzeju",
                "https://www.coupang.com/vp/products/220719112?itemId=688523681&vendorItemId=4765825003&q=테아닌&searchId=72c2b80111887548&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijyzx16",
                "https://www.coupang.com/vp/products/2839766?itemId=13196072&vendorItemId=3272346343&q=테아닌&searchId=72c2b80111887548&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijz08ki"
        });
        NUTRIENT_LINKS.put("판토텐산", new String[]{
                "https://www.coupang.com/vp/products/7007680965?itemId=6160351716&vendorItemId=89967646175&q=판토텐산&searchId=448b923b5168595&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz0t96",
                "https://www.coupang.com/vp/products/6978767285?itemId=1087801&vendorItemId=3179536026&pickType=COU_PICK&q=판토텐산&searchId=448b923b5168595&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijz1lc2",
                "https://www.coupang.com/vp/products/8868409218?itemId=25870113994&vendorItemId=92856278416&q=판토텐산&searchId=448b923b5168595&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijz20h6"
        });
        NUTRIENT_LINKS.put("포스파티딜세린", new String[]{
                "https://www.coupang.com/vp/products/1221154935?itemId=17001979386&vendorItemId=70209793223&q=포스파티딜세린&searchId=952e2bfa3650762&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz3262",
                "https://www.coupang.com/vp/products/3250936?itemId=65397&vendorItemId=3275509624&q=포스파티딜세린&searchId=952e2bfa3650762&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijz3gki",
                "https://www.coupang.com/vp/products/8974662005?itemId=26269864975&vendorItemId=93248035980&q=포스파티딜세린&searchId=952e2bfa3650762&sourceType=search&itemsCount=36&searchRank=14&rank=14&traceId=mijz486q"
        });
        NUTRIENT_LINKS.put("유산균", new String[]{
                "https://www.coupang.com/vp/products/2638550?itemId=12788104766&vendorItemId=3265972233&q=프로바이오틱스&searchId=7bade12c9238063&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz5wb6",
                "https://www.coupang.com/vp/products/42701778?itemId=20157758375&vendorItemId=3361324172&q=프로바이오틱스&searchId=7bade12c9238063&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijz69ma",
                "https://www.coupang.com/vp/products/8678602014?itemId=25192959189&vendorItemId=92189499441&q=프로바이오틱스&searchId=7bade12c9238063&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijz6mqa"
        });
        NUTRIENT_LINKS.put("홍경천", new String[]{
                "https://www.coupang.com/vp/products/356512?itemId=19303389993&vendorItemId=88949880103&q=홍경천&searchId=49286223677896&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz76p6",
                "https://www.coupang.com/vp/products/9203905305?itemId=27178288004&vendorItemId=94145899640&q=홍경천&searchId=49286223677896&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijz81mb",
                "https://www.coupang.com/vp/products/216731325?itemId=666484784&vendorItemId=71973188799&q=홍경천&searchId=49286223677896&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijz928j"
        });
        NUTRIENT_LINKS.put("히알루론산", new String[]{
                "https://www.coupang.com/vp/products/65095217?itemId=219977034&vendorItemId=3531370031&pickType=COU_PICK&q=히알루론산&searchId=bb6dc9d44030937&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijza6nn",
                "https://www.coupang.com/vp/products/6453425570?itemId=14018527969&vendorItemId=81267058726&q=히알루론산&searchId=bb6dc9d44030937&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijzagua",
                "https://www.coupang.com/vp/products/1581894292?itemId=5917029534&vendorItemId=70694649964&q=히알루론산&searchId=bb6dc9d44030937&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijzb5ve"
        });
        NUTRIENT_LINKS.put("MSM", new String[]{
                "https://www.coupang.com/vp/products/8289188131?itemId=17973906406&vendorItemId=3272699133&q=MSM&searchId=17aecb173873696&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijzbot7",
                "https://www.coupang.com/vp/products/7260495266?itemId=18492046431&vendorItemId=70903898461&q=MSM&searchId=17aecb173873696&sourceType=search&itemsCount=36&searchRank=6&rank=6&traceId=mijzckle",
                "https://www.coupang.com/vp/products/7621638805?itemId=14404871528&vendorItemId=76495478225&q=MSM&searchId=17aecb173873696&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijzd2iy"
        });

        // 6. 이미지 데이터 초기화
        NUTRIENT_IMAGES.put("비타민 A", "비타민 A1.jpg");
        NUTRIENT_IMAGES.put("비타민 B1", "비타민 B11.jpg");
        NUTRIENT_IMAGES.put("비타민 B2", "비타민 B21.jpg");
        NUTRIENT_IMAGES.put("비타민 C", "비타민 C1.jpg");
        NUTRIENT_IMAGES.put("비타민 D", "비타민 D1.jpg");
        NUTRIENT_IMAGES.put("비타민 E", "비타민 E1.jpg");
        NUTRIENT_IMAGES.put("비타민 B군", "비타민 B군1.jpg");
        NUTRIENT_IMAGES.put("판토텐산", "판토텐산1.jpg");
        NUTRIENT_IMAGES.put("엽산", "엽산1.jpg");
        NUTRIENT_IMAGES.put("칼슘", "칼슘1.jpg");
        NUTRIENT_IMAGES.put("마그네슘", "마그네슘1.jpg");
        NUTRIENT_IMAGES.put("철분", "철분1.jpg");
        NUTRIENT_IMAGES.put("아연", "아연1.jpg");
        NUTRIENT_IMAGES.put("루테인",  "루테인1.jpg");
        NUTRIENT_IMAGES.put("오메가3", "오메가31.jpg");
        NUTRIENT_IMAGES.put("유산균", "유산균1.jpg");
        NUTRIENT_IMAGES.put("밀크씨슬", "밀크씨슬1.jpg");
        NUTRIENT_IMAGES.put("MSM", "MSM1.jpg");
        NUTRIENT_IMAGES.put("홍경천", "홍경천1.jpg");
        NUTRIENT_IMAGES.put("테아닌", "테아닌1.jpg");
        NUTRIENT_IMAGES.put("포스파티딜세린", "포스파티딜세린1.jpg");
        NUTRIENT_IMAGES.put("가르시니아", "가르시니아1.jpg");
        NUTRIENT_IMAGES.put("달맞이꽃종자유", "달맞이꽃종자유1.jpg");
        NUTRIENT_IMAGES.put("히알루론산", "히알루론산1.jpg");
        NUTRIENT_IMAGES.put("비오틴", "비오틴1.jpg");

        // 만약 이미지 키가 없을 때 사용할 기본값 추가
        NUTRIENT_IMAGES.put("DEFAULT", "images/default_nutrient.jpg");

        // 7. 구매 링크에 맞는 제품 사진
        // 가르시니아
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6697513406?itemId=21224855184&vendorItemId=73464152763&sourceType=srp_product_ads&clickEventId=90f1a150-ccef-11f0-9992-2b04fdd17277&korePlacement=15&koreSubPlacement=1&clickEventId=90f1a150-ccef-11f0-9992-2b04fdd17277&korePlacement=15&koreSubPlacement=1&traceId=mijxlrju", "가르니시아1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6807918291?itemId=24020097347&vendorItemId=3028473540&q=가르시니아&searchId=50e8bf993278528&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijxoabt", "가르니시아2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6681304496?itemId=15407541825&vendorItemId=70056438557&sourceType=srp_product_ads&clickEventId=90f1c860-ccef-11f0-953b-404927f5581d&korePlacement=15&koreSubPlacement=5&clickEventId=90f1c860-ccef-11f0-953b-404927f5581d&korePlacement=15&koreSubPlacement=5&traceId=mijxol0p","가르니시아3.jpg");

        // 달맞이꽃종자유
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6789736242?itemId=18437189193&vendorItemId=3000040918&pickType=COU_PICK&q=달맞이꽃종자유&searchId=ce7d708911707448&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijxqx41", "달맞이꽃종자유1");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7338313739?itemId=25780653324&vendorItemId=92878998817&q=달맞이꽃종자유&searchId=ce7d708911707448&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijxs56p", "달맞이꽃종자유2");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7788207539?itemId=21064308808&vendorItemId=3000279175&q=달맞이꽃종자유&searchId=ce7d708911707448&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijxsd8h", "달맞이꽃종자유3");

        // 루테인
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8936251513?itemId=26127558615&vendorItemId=93687865713&q=루테인&searchId=29d8d55b489507&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijxwitd", "루테인1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/171811?itemId=18204786075&vendorItemId=86203373195&q=루테인&searchId=29d8d55b489507&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijxx6qh", "루테인2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8181315530?itemId=23541389413&vendorItemId=91690674857&sourceType=srp_product_ads&clickEventId=b2f121d0-ccf0-11f0-9c80-56675a98e1ae&korePlacement=15&koreSubPlacement=11&clickEventId=b2f121d0-ccf0-11f0-9c80-56675a98e1ae&korePlacement=15&koreSubPlacement=11&traceId=mijxxktu", "루테인3.jpg");

        // 마그네슘
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7792384678?itemId=21084993184&vendorItemId=79415792989&q=마그네슘&searchId=3b23800f3708400&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijxyf82", "마그네슘1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6527425583?itemId=14485037757&vendorItemId=76228037698&q=마그네슘&searchId=3b23800f3708400&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijxz0o1", "마그네슘2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1366689007?itemId=23951551099&vendorItemId=94119849475&q=마그네슘&searchId=3b23800f3708400&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijxzfo1", "마그네슘3.jpg");

        // 밀크씨슬
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7156986260?itemId=13254294391&vendorItemId=3180741512&q=밀크씨슬&searchId=650b2c673332192&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijy1b4y", "밀크씨슬1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6703806340?itemId=18989222194&vendorItemId=88618091836&q=밀크씨슬&searchId=650b2c673332192&sourceType=search&itemsCount=36&searchRank=16&rank=16&traceId=mijy1yd5", "밀크씨슬2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7346382208?itemId=15003949840&vendorItemId=92127531279&sourceType=srp_product_ads&clickEventId=44eeca10-ccf1-11f0-894a-639ccbe6e697&korePlacement=15&koreSubPlacement=9&clickEventId=44eeca10-ccf1-11f0-894a-639ccbe6e697&korePlacement=15&koreSubPlacement=9&traceId=mijy3mpt", "밀크씨슬3.jpg");

        // 비오틴
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1955584555?itemId=23409820872&vendorItemId=72495291625&q=비오틴&searchId=02d4497e523860&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijy4eoi", "비오틴1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7141471981?itemId=17930490564&vendorItemId=85092793761&q=비오틴&searchId=02d4497e523860&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijy5a8h", "비오틴2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/220719097?itemId=688523660&vendorItemId=4765824817&sourceType=SDW_TOP_SELLING_WIDGET_V2&searchId=02d4497e523860&q=비오틴", "비오틴3.jpg");

        // 비타민 A
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2878672?itemId=13411639&vendorItemId=3272326293&q=비타민+A&searchId=f6807d682740812&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijy6x7e", "비타민 A1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/216731272?itemId=666484606&vendorItemId=4722784812&q=비타민+A&searchId=f6807d682740812&sourceType=search&itemsCount=36&searchRank=19&rank=19&traceId=mijy7q7m", "비타민 A2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6739267330?itemId=15726380856&vendorItemId=3253073418&q=비타민+A&searchId=f6807d682740812&sourceType=search&itemsCount=36&searchRank=30&rank=30&traceId=mijy94n5", "비타민 A3.jpg");

        // 비타민 B1
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8289756716?itemId=12115396226&vendorItemId=71973191016&sourceType=srp_product_ads&clickEventId=2fec1b80-ccf2-11f0-82d5-7f49eb08a1d2&korePlacement=15&koreSubPlacement=1&clickEventId=2fec1b80-ccf2-11f0-82d5-7f49eb08a1d2&korePlacement=15&koreSubPlacement=1&traceId=mijy9qbt", "비타민 B11.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/28024?itemId=68076&vendorItemId=3086634180&q=비타민+B1&searchId=2307dbf13367743&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyao9m", "비타민 B12.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/3248548?itemId=2358605063&vendorItemId=3273080985&q=비타민+B1&searchId=2307dbf13367743&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijybbpd", "비타민 B13.jpg");

        // 비타민 B2
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/216731274?itemId=666484610&vendorItemId=4722784826&q=비타민+B2&searchId=d4b13d2e2761049&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyc07m", "비타민 B21.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8075002605?itemId=24174618743&vendorItemId=92932633520&q=비타민+B2&searchId=d4b13d2e2761049&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijycq7u", "비타민 B22.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/28026?itemId=68078&vendorItemId=88071243392&q=비타민+B2&searchId=d4b13d2e2761049&sourceType=search&itemsCount=36&searchRank=5&rank=5&traceId=mijydaz6", "비타민 B23.jpg");

        // 비타민 B군
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8289158533?itemId=18169956995&vendorItemId=84910700916&q=비타민+B군&searchId=a647a63c547010&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijydzca", "비타민 B군1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8136129840?itemId=9354802&vendorItemId=3180840912&q=비타민+B군&searchId=a647a63c547010&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyeqf5", "비타민 B군2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7449869595?itemId=19391986964&vendorItemId=74820920119&q=비타민+B군&searchId=a647a63c547010&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyf70a", "비타민 B군3.jpg");

        // 비타민 C
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/9192490440?itemId=16777795279&vendorItemId=3000235591&pickType=COU_PICK&q=비타민+C&searchId=e52bc1f94000926&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyg15e", "비타민 C1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6903873562?itemId=1088946&vendorItemId=3001047907&q=비타민+C&searchId=e52bc1f94000926&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijygc0q", "비타민 C2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/9095157389?itemId=26981112155&vendorItemId=88472838413&sourceType=srp_product_ads&clickEventId=dcda95b0-ccf2-11f0-9f5c-b7c784183813&korePlacement=15&koreSubPlacement=5&clickEventId=dcda95b0-ccf2-11f0-9f5c-b7c784183813&korePlacement=15&koreSubPlacement=5&traceId=mijyhdfu", "비타민 C3.jpg");

        // 비타민 D
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/21912752?itemId=85422647&vendorItemId=3145132773&q=비타민+D&searchId=e2bf0cb72904901&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyihoa", "비타민 D1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1780508524?itemId=3031702911&vendorItemId=4722784677&q=비타민+D&searchId=e2bf0cb72904901&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyj8de", "비타민 D2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/220719096?itemId=688523657&vendorItemId=4765824832&q=비타민+D&searchId=e2bf0cb72904901&sourceType=search&itemsCount=36&searchRank=14&rank=14&traceId=mijyjm4p", "비타민 D3.jpg");

        // 비타민 E
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1581929897?itemId=2704389446&vendorItemId=70694701289&q=비타민+E&searchId=70c58c2a3994170&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijykeje", "비타민 E1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2878792?itemId=13412176&vendorItemId=3272319048&q=비타민+E&searchId=70c58c2a3994170&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyl5oy", "비타민 E2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/587394?itemId=70353&vendorItemId=3086364645&q=비타민+E&searchId=70c58c2a3994170&sourceType=search&itemsCount=36&searchRank=8&rank=8&traceId=mijymn8q", "비타민 E3.jpg");

// 아연
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6215299433?itemId=1184751&vendorItemId=3001141888&q=아연&searchId=4f82d07711843317&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijynxu2", "아연1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2878729?itemId=13411906&vendorItemId=3272333667&q=아연&searchId=4f82d07711843317&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyol02", "아연2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7924178640?itemId=22812356618&vendorItemId=89847224757&q=아연&searchId=4f82d07711843317&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyozg2", "아연3.jpg");

// 엽산
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/29304?itemId=70356&vendorItemId=3086361478&q=엽산&searchId=191a7dbd592515&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijypn6a", "엽산1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7823882369?itemId=21258939802&vendorItemId=3001142329&pickType=COU_PICK&q=엽산&searchId=191a7dbd592515&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyqe0y", "엽산2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2878679?itemId=13411667&vendorItemId=3272327980&q=엽산&searchId=191a7dbd592515&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijyqr1e", "엽산3.jpg");

// 오메가3
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6854281548?itemId=24023322885&vendorItemId=4765824687&q=오메가3&searchId=ca212620599945&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijyrgxe", "오메가31.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7821106059?itemId=21244482032&vendorItemId=3000085957&q=오메가3&searchId=ca212620599945&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijyscsq", "오메가32.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7559679373?itemId=10076200032&vendorItemId=70393847077&q=오메가3&searchId=aae86a41617377&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyt9my", "오메가33.jpg");

// 철분
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1181654?itemId=5108920&vendorItemId=3006347663&pickType=COU_PICK&q=철분&searchId=1a6dc623620472&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijytvsx", "철분1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/3239450?itemId=14966971&vendorItemId=3273248238&q=철분&searchId=1a6dc623620472&sourceType=search&itemsCount=36&searchRank=12&rank=12&traceId=mijyuhte", "철분2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2557580?itemId=24043582673&vendorItemId=88071309863&q=철분&searchId=1a6dc623620472&sourceType=search&itemsCount=36&searchRank=23&rank=23&traceId=mijyx08i", "철분3.jpg");

// 칼슘
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8289193023?itemId=70377&vendorItemId=3086591291&q=칼슘&searchId=550aad1a2376275&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyxn2r", "칼슘1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8289178801?itemId=9357481&vendorItemId=3267477799&q=칼슘&searchId=550aad1a2376275&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijyxwsa", "칼슘2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6509173131?itemId=14369677042&vendorItemId=81614046301&q=칼슘&searchId=550aad1a2376275&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijyy8yh", "칼슘3.jpg");

// 테아닌
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8289046224?itemId=17187727471&vendorItemId=4722785945&q=테아닌&searchId=72c2b80111887548&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijyzeju", "테아닌1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/220719112?itemId=688523681&vendorItemId=4765825003&q=테아닌&searchId=72c2b80111887548&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijyzx16", "테아닌2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2839766?itemId=13196072&vendorItemId=3272346343&q=테아닌&searchId=72c2b80111887548&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijz08ki", "테아닌3.jpg");

// 판토텐산
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7007680965?itemId=6160351716&vendorItemId=89967646175&q=판토텐산&searchId=448b923b5168595&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz0t96", "판토텐산1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6978767285?itemId=1087801&vendorItemId=3179536026&pickType=COU_PICK&q=판토텐산&searchId=448b923b5168595&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijz1lc2", "판토텐산2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8868409218?itemId=25870113994&vendorItemId=92856278416&q=판토텐산&searchId=448b923b5168595&sourceType=search&itemsCount=36&searchRank=3&rank=3&traceId=mijz20h6", "판토텐산3.jpg");

// 포스파티딜세린
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1221154935?itemId=17001979386&vendorItemId=70209793223&q=포스파티딜세린&searchId=952e2bfa3650762&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz3262", "포스파티딜세린1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/3250936?itemId=65397&vendorItemId=3275509624&q=포스파티딜세린&searchId=952e2bfa3650762&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijz3gki", "포스파티딜세린2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8974662005?itemId=26269864975&vendorItemId=93248035980&q=포스파티딜세린&searchId=952e2bfa3650762&sourceType=search&itemsCount=36&searchRank=14&rank=14&traceId=mijz486q", "포스파티딜세린3.jpg");

// 유산균
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/2638550?itemId=12788104766&vendorItemId=3265972233&q=프로바이오틱스&searchId=7bade12c9238063&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz5wb6", "유산균1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/42701778?itemId=20157758375&vendorItemId=3361324172&q=프로바이오틱스&searchId=7bade12c9238063&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijz69ma", "유산균2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8678602014?itemId=25192959189&vendorItemId=92189499441&q=프로바이오틱스&searchId=7bade12c9238063&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijz6mqa", "유산균3.jpg");

// 홍경천
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/356512?itemId=19303389993&vendorItemId=88949880103&q=홍경천&searchId=49286223677896&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijz76p6", "홍경천1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/9203905305?itemId=27178288004&vendorItemId=94145899640&q=홍경천&searchId=49286223677896&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijz81mb", "홍경천2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/216731325?itemId=666484784&vendorItemId=71973188799&q=홍경천&searchId=49286223677896&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijz928j", "홍경천3.jpg");

// 히알루론산
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/65095217?itemId=219977034&vendorItemId=3531370031&pickType=COU_PICK&q=히알루론산&searchId=bb6dc9d44030937&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijza6nn", "히알루론산1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/6453425570?itemId=14018527969&vendorItemId=81267058726&q=히알루론산&searchId=bb6dc9d44030937&sourceType=search&itemsCount=36&searchRank=2&rank=2&traceId=mijzagua", "히알루론산2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/1581894292?itemId=5917029534&vendorItemId=70694649964&q=히알루론산&searchId=bb6dc9d44030937&sourceType=search&itemsCount=36&searchRank=7&rank=7&traceId=mijzb5ve", "히알루론산3.jpg");

// MSM
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/8289188131?itemId=17973906406&vendorItemId=3272699133&q=MSM&searchId=17aecb173873696&sourceType=search&itemsCount=36&searchRank=1&rank=1&traceId=mijzbot7", "MSM1.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7260495266?itemId=18492046431&vendorItemId=70903898461&q=MSM&searchId=17aecb173873696&sourceType=search&itemsCount=36&searchRank=6&rank=6&traceId=mijzckle", "MSM2.jpg");
        PRODUCT_IMAGES.put("https://www.coupang.com/vp/products/7621638805?itemId=14404871528&vendorItemId=76495478225&q=MSM&searchId=17aecb173873696&sourceType=search&itemsCount=36&searchRank=9&rank=9&traceId=mijzd2iy", "MSM3.jpg");

    }

    // [헬퍼 메서드] 3가지 맵에 데이터를 한 번에 넣기 위함
    private static void addNutrientInfo(String name, String good, String bad, String time) {
        GOOD_COMBINATIONS.put(name, good);
        BAD_COMBINATIONS.put(name, bad);
        INTAKE_TIPS.put(name, time);
    }

    // --- 메서드 ---
    public Set<String> getRecommendedNutrients(List<String> selectedSymptoms) {
        Set<String> uniqueNutrients = new LinkedHashSet<>();
        for (String symptom : selectedSymptoms) {
            List<String> nutrients = YOUNG_YANG.get(symptom);
            if (nutrients != null) uniqueNutrients.addAll(nutrients);
        }
        return uniqueNutrients;
    }

    public String getNutrientImage(String nutrientName) {
        String imagePath = NUTRIENT_IMAGES.get(nutrientName);
        if (imagePath == null) {
            return NUTRIENT_IMAGES.get("DEFAULT");
        }
        return imagePath;
    }

    // [중요] 팝업에서 쓸 정보를 가져오는 메서드 (세분화된 정보 취합)
    public List<String> getRecommendations(List<String> selectedSymptoms) {
        List<String> recommendations = new ArrayList<>();
        Set<String> uniqueNutrients = getRecommendedNutrients(selectedSymptoms);

        if (uniqueNutrients.isEmpty()) {
            return recommendations;
        }

        for (String nutrient : uniqueNutrients) {
            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("【 ").append(nutrient).append(" 】\n");

            // 1. 상세 설명
            String[] details = getNutrientDetails(nutrient);
            if (details != null && details.length >= 2) {
                sb.append("\n상세 설명:\n").append(details[1]).append("\n");
            }

            // 2. 같이 먹으면 좋은 성분
            String good = GOOD_COMBINATIONS.get(nutrient);
            if (good != null) {
                sb.append("\n같이 먹으면 좋은 성분 (시너지):\n").append(good).append("\n");
            }

            // 3. 같이 먹으면 안 좋은 성분
            String bad = BAD_COMBINATIONS.get(nutrient);
            if (bad != null) {
                sb.append("\n같이 먹으면 안 좋은 성분:\n").append(bad).append("\n");
            }

            // 4. 섭취 시간 팁
            String time = INTAKE_TIPS.get(nutrient);
            if (time != null) {
                sb.append("\n섭취 시간 팁:\n").append(time).append("\n");
            }

            recommendations.add(sb.toString());
        }

        return recommendations;
    }

    // Getter 메서드들 (NutrientDetailPopup 등에서 개별적으로 호출 가능)
    public static String[] getNutrientDetails(String nutrientName) {
        return YOUNG_YANG_EXPLAIN.get(nutrientName);
    }

    public static String getGoodCombo(String nutrientName) {
        return GOOD_COMBINATIONS.get(nutrientName);
    }

    public static String getBadCombo(String nutrientName) {
        return BAD_COMBINATIONS.get(nutrientName);
    }

    public static String getIntakeTip(String nutrientName) { // 여기서는 '시간' 정보만 리턴됨
        return INTAKE_TIPS.get(nutrientName);
    }

    public static String[] getNutrientLinks(String nutrientName) {
        return NUTRIENT_LINKS.getOrDefault(nutrientName, new String[]{"#", "#", "#"});
    }

    public static Set<String> getAllNutrientNames() {
        return YOUNG_YANG_EXPLAIN.keySet();
    }

    public static List<String> getAllSymptoms() {
        return new ArrayList<>(YOUNG_YANG.keySet());
    }

    // URL을 넣으면 매핑된 제품 이미지 파일명을 반환하는 메서드
    public static String getProductImage(String url) {
        // 매핑된 이미지가 없으면 기본 이미지 반환
        return PRODUCT_IMAGES.getOrDefault(url, "images/default_product.jpg");
    }
}