package com.pawtrail.verdict.domain.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * 조건 20칸입니다. 이유 줄의 칸 이름과 화면에 보이는 칸 이름표를 함께 가집니다.
 *
 * <b>이름표는 policy 의 FieldSpec 과 같은 말입니다.</b>
 * 장소 상세에 policy 의 조건 충돌 목록과 이 서비스의 이유 줄이 나란히 뜨므로 같은 칸은 같은 말이어야 합니다.
 * 두 레포가 달라 자동으로 맞춰 보지 못하므로, policy 의 이름표가 바뀌면 여기도 함께 고칩니다.
 *
 * 선언 순서가 칸 순서입니다. 결과가 같은 이유 줄은 이 순서로 늘어놓습니다.
 */
public enum ConditionField {

    SCOPE("scope", "동반 범위"),
    GUIDE_DOG_ONLY("guideDogOnly", "안내견 한정"),
    PET_ONLY("petOnly", "반려견 동반 전용"),
    INDOOR_ALLOWED("indoorAllowed", "실내 동반"),
    OUTDOOR_ALLOWED("outdoorAllowed", "실외 동반"),
    MAX_WEIGHT_KG("maxWeightKg", "체중 제한"),
    WEIGHT_INCLUSIVE("weightInclusive", "체중 기준"),
    MAX_COUNT("maxCount", "마릿수 제한"),
    SIZE_RULE("sizeRule", "크기 제한"),
    BREED_RULE("breedRule", "견종 제한"),
    CARRIER_REQUIRED("carrierRequired", "이동장"),
    LEASH_REQUIRED("leashRequired", "목줄"),
    EXCLUDED_ZONES("excludedZones", "동반 불가 구역"),
    ALLOWED_ZONES_ONLY("allowedZonesOnly", "동반 가능 구역"),
    EXCLUDED_DAYS("excludedDays", "동반 불가일"),
    EXTRA_FEE_AMOUNT("extraFeeAmount", "추가 요금"),
    EXTRA_FEE_UNIT("extraFeeUnit", "요금 기준"),
    REQUIRED_ITEMS("requiredItems", "준비물"),
    VACCINE_PROOF("vaccineProof", "접종 증명"),
    ADVANCE_INQUIRY("advanceInquiry", "사전 문의");

    // policy 가 근거 줄에 싣는 칸 이름 — camelCase 이며 DB 컬럼 이름이 아님
    private final String fieldName;

    private final String label;

    ConditionField(String fieldName, String label) {
        this.fieldName = fieldName;
        this.label = label;
    }

    public String fieldName() {
        return fieldName;
    }

    public String label() {
        return label;
    }

    /**
     * 근거 줄의 칸 이름으로 찾습니다. 모르는 이름이면 비어 있습니다.
     */
    public static Optional<ConditionField> ofName(String fieldName) {
        return Arrays.stream(values())
                .filter(field -> field.fieldName.equals(fieldName))
                .findFirst();
    }
}
