package com.pawtrail.verdict.domain.rule;

import com.pawtrail.verdict.domain.enums.BreedRule;
import com.pawtrail.verdict.domain.enums.ConditionField;
import com.pawtrail.verdict.domain.enums.ReasonStatus;
import com.pawtrail.verdict.domain.enums.Scope;
import com.pawtrail.verdict.domain.enums.SizeRule;
import com.pawtrail.verdict.domain.enums.Verdict;
import com.pawtrail.verdict.domain.model.Conditions;
import com.pawtrail.verdict.domain.model.EvidenceLine;
import com.pawtrail.verdict.domain.model.PetJudgement;
import com.pawtrail.verdict.domain.model.PetProfile;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import com.pawtrail.verdict.domain.model.Reason;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 한 장소에서 반려동물 한 마리의 판정을 냅니다.
 *
 * 조건 칸마다 이유 줄을 만들고, 줄의 결과를 모아 판정을 정합니다. 앞선 단계가 이깁니다.
 *
 * <pre>
 * 불가       범위 동반 불가 · 실내 · 실외 둘 다 불가 · 안내견 한정 · 크기 · 체중이 막음 · 맹견 불가(맹견일 때)
 *           이동장 필요인데 이동장 · 유모차가 없음 · 접종 증명 필요인데 증명서가 없음
 * 확인 필요   조건 정보 없음 · 동반 자체를 모름 · 크기 · 체중이 둘 다 빔 · 맹견 규칙이 빔(맹견일 때) · 반려동물 없음
 *           · 맹견인지 모름(맹견 규칙이 제한 없음이 아닐 때)
 * 조건부     일부 구역 · 실내만 · 실외만 · 허용 구역 · 제외 구역 · 제외 요일 · 사전 문의
 *           · 이동장 필요(갖춤) · 접종 증명 필요(갖춤) · 맹견 입마개(맹견일 때)
 * 참고       반려견 동반 전용 · 마릿수 · 목줄 · 추가 요금 · 준비물 — 판정에 영향 없음
 * </pre>
 *
 * <b>모르는 것을 가능으로 올리지 않습니다.</b> 조건의 상당수를 모델이 안내문에서 읽으므로,
 * 판정에 필요한 칸이 비면 "가능" 이 아니라 "확인 필요" 로 냅니다.
 * 다만 이동장 · 접종 증명처럼 원문이 말하는 곳이 드문 칸은 비어 있어도 막지 않습니다.
 * 막으면 거의 모든 장소가 확인 필요가 되어 판정이 뜻을 잃습니다.
 *
 * <b>조건부는 "그대로 가면 막힐 수 있으니 이유를 읽으라" 는 뜻입니다.</b>
 * 구역 · 요일 · 사전 문의처럼 장소가 거는 제한과, 이동장 · 증명서 · 입마개처럼 입구에서 갖춰야 하는 것이 여기 듭니다.
 * 목줄 · 준비물 · 요금까지 올리면 동반되는 곳 대부분이 조건부가 되어 정말 읽어야 할 곳이 묻힙니다.
 *
 * <b>마릿수는 판정에 쓰지 않습니다.</b> 판정은 한 마리씩 내는데 마릿수는 함께 가는 무리에 걸리는 조건입니다.
 * 부르는 쪽이 넘기는 반려동물 목록은 함께 가는 무리가 아니므로(일정은 날마다 다른 반려동물을 모아 부름)
 * 참고 줄로만 보이고, 함께 가는 마릿수를 아는 화면이 견줍니다.
 *
 * 스프링을 모르는 순수 계산이라 단위 테스트로 검증합니다.
 */
public final class VerdictJudge {

    private VerdictJudge() {
    }

    /**
     * @param petId 판정할 반려동물. 반려동물 정보가 없어도 이 식별자로 결과를 냄
     * @param pet   pet 에서 받은 정보 — 없거나 남의 것이면 null
     * @param place policy 에서 받은 조건 — 조건 행이 없는 장소면 null
     */
    public static PetJudgement judge(UUID petId, PetProfile pet, PlaceConditions place) {
        Lines lines = new Lines(place == null ? List.of() : place.evidence());
        if (pet == null) {
            // 없거나 남의 반려동물 — 누구 것이든 같은 값을 내 있는지 없는지가 새지 않음
            lines.addMissingPet();
        } else if (place == null) {
            lines.add(ConditionField.SCOPE, ReasonStatus.MISSING, "동반 조건 정보 없음");
        } else {
            Conditions conditions = place.conditions();
            access(conditions, lines);
            guideDog(conditions, lines);
            sizeAndWeight(conditions, pet, lines);
            breed(conditions, pet, lines);
            carrier(conditions, pet, lines);
            vaccine(conditions, pet, lines);
            zonesAndDays(conditions, lines);
            notes(conditions, lines);
        }
        List<Reason> reasons = lines.sorted();
        return new PetJudgement(petId, verdictOf(reasons), reasons);
    }

    /**
     * 줄들의 결과 가운데 가장 센 것입니다.
     */
    static Verdict verdictOf(List<Reason> reasons) {
        Verdict verdict = Verdict.ALLOWED;
        for (Reason reason : reasons) {
            verdict = verdict.stricter(reason.status().toVerdict());
        }
        return verdict;
    }

    /**
     * 동반 가부 — 범위 · 실내 · 실외를 한 묶음으로 봅니다.
     *
     * 범위가 동반 불가면 그 한 줄만 둡니다. extract 가 이때 실내 · 실외도 불가로 채우므로 줄을 더 두면 같은 말이 세 번 나옵니다.
     * 어디서도 된다는 말이 없으면 동반 자체를 모르는 것입니다. 실내만 불가라고 적혀 있어도 실외가 되는지 모릅니다.
     * 한쪽만 되는 곳은 안 되는 쪽을 조건부로 두고, 다른 쪽이 된다고 적혀 있으면 그쪽에서만 된다고 밝힙니다.
     */
    private static void access(Conditions conditions, Lines lines) {
        Scope scope = conditions.scope() == Scope.UNKNOWN ? null : conditions.scope();
        Boolean indoor = conditions.indoorAllowed();
        Boolean outdoor = conditions.outdoorAllowed();

        if (scope == Scope.NONE) {
            lines.add(ConditionField.SCOPE, ReasonStatus.NOT_MET, "동반 불가");
            return;
        }
        if (Boolean.FALSE.equals(indoor) && Boolean.FALSE.equals(outdoor)) {
            lines.add(ConditionField.INDOOR_ALLOWED, ReasonStatus.NOT_MET, "불가");
            lines.add(ConditionField.OUTDOOR_ALLOWED, ReasonStatus.NOT_MET, "불가");
            return;
        }

        boolean allowedSomewhere = scope == Scope.ALL_AREA || scope == Scope.PARTIAL
                || Boolean.TRUE.equals(indoor) || Boolean.TRUE.equals(outdoor);
        if (!allowedSomewhere) {
            lines.add(ConditionField.SCOPE, ReasonStatus.MISSING, "동반 가능 여부 정보 없음");
        } else if (scope == Scope.PARTIAL) {
            lines.add(ConditionField.SCOPE, ReasonStatus.CONDITION, "일부 구역만 동반 가능");
        } else if (scope == Scope.ALL_AREA) {
            lines.add(ConditionField.SCOPE, ReasonStatus.MET, "전 구역 동반 가능");
        }
        space(ConditionField.INDOOR_ALLOWED, indoor, outdoor, "실외", lines);
        space(ConditionField.OUTDOOR_ALLOWED, outdoor, indoor, "실내", lines);
    }

    // 실내 · 실외 한 칸 — 되면 충족, 안 되면 조건부 · 비어 있으면 줄을 두지 않음
    private static void space(ConditionField field, Boolean value, Boolean other, String otherName, Lines lines) {
        if (Boolean.TRUE.equals(value)) {
            lines.add(field, ReasonStatus.MET, "가능");
        } else if (Boolean.FALSE.equals(value)) {
            lines.add(field, ReasonStatus.CONDITION,
                    Boolean.TRUE.equals(other) ? "불가 — " + otherName + "에서만 동반 가능" : "불가");
        }
    }

    // pet 에 안내견인지 담는 칸이 없어 안내견 한정인 곳은 모든 반려견을 막음
    private static void guideDog(Conditions conditions, Lines lines) {
        if (Boolean.TRUE.equals(conditions.guideDogOnly())) {
            lines.add(ConditionField.GUIDE_DOG_ONLY, ReasonStatus.NOT_MET, "안내견만 입장 가능");
        }
    }

    /**
     * 크기와 체중 — 모든 반려견에게 필요한 칸이라 둘 다 비면 확인 필요입니다.
     *
     * 둘 다 적혀 있으면 둘 다 봅니다. 체중이 상한과 같은데 "이하" 인지 "미만" 인지 모르면 확인 필요로 둡니다.
     */
    private static void sizeAndWeight(Conditions conditions, PetProfile pet, Lines lines) {
        SizeRule size = conditions.sizeRule();
        BigDecimal limit = conditions.maxWeightKg();
        if (size == null && limit == null) {
            lines.add(ConditionField.SIZE_RULE, ReasonStatus.MISSING, "크기 · 체중 조건 정보 없음");
            return;
        }

        if (size == SizeRule.ALL) {
            lines.add(ConditionField.SIZE_RULE, ReasonStatus.MET, size.text());
        } else if (size != null && pet.breedSize() == null) {
            lines.add(ConditionField.SIZE_RULE, ReasonStatus.MISSING, "반려견 크기 정보 없음 — " + size.text());
        } else if (size != null) {
            ReasonStatus status = size.accepts(pet.breedSize()) ? ReasonStatus.MET : ReasonStatus.NOT_MET;
            lines.add(ConditionField.SIZE_RULE, status, petSize(pet) + " — " + size.text());
        }

        if (limit == null) {
            return;
        }
        Boolean inclusive = conditions.weightInclusive();
        String rule = kg(limit) + (inclusive == null ? "" : inclusive ? " 이하" : " 미만");
        if (pet.weightKg() == null) {
            lines.add(ConditionField.MAX_WEIGHT_KG, ReasonStatus.MISSING, "반려견 체중 정보 없음 — " + rule,
                    ConditionField.WEIGHT_INCLUSIVE);
            return;
        }
        int compared = pet.weightKg().compareTo(limit);
        String message = kg(pet.weightKg()) + " — " + rule;
        ReasonStatus status;
        if (compared < 0) {
            status = ReasonStatus.MET;
        } else if (compared > 0) {
            status = ReasonStatus.NOT_MET;
        } else if (inclusive == null) {
            status = ReasonStatus.MISSING;
            message = message + " (이하인지 미만인지 정보 없음)";
        } else {
            status = inclusive ? ReasonStatus.MET : ReasonStatus.NOT_MET;
        }
        lines.add(ConditionField.MAX_WEIGHT_KG, status, message, ConditionField.WEIGHT_INCLUSIVE);
    }

    // 맹견 규칙은 맹견에만 걸림 — 맹견이 아니면 줄을 두지 않음
    // 맹견인지 모르면(pet 응답에 칸이 없음) 제한 없음이 아닌 한 확인 필요 — 맹견 불가인지 입마개인지를 가를 수 없음
    private static void breed(Conditions conditions, PetProfile pet, Lines lines) {
        Boolean dangerous = pet.dangerousBreed();
        if (Boolean.FALSE.equals(dangerous)) {
            return;
        }
        BreedRule rule = conditions.breedRule();
        if (dangerous == null) {
            if (rule != BreedRule.NONE) {
                lines.add(ConditionField.BREED_RULE, ReasonStatus.MISSING,
                        rule == null ? "반려견의 맹견 여부 정보 없음" : "반려견의 맹견 여부 정보 없음 — " + rule.text());
            }
            return;
        }
        if (rule == null) {
            lines.add(ConditionField.BREED_RULE, ReasonStatus.MISSING, "맹견 동반 조건 정보 없음");
            return;
        }
        ReasonStatus status = switch (rule) {
            case DANGEROUS_BANNED -> ReasonStatus.NOT_MET;
            case DANGEROUS_MUZZLE -> ReasonStatus.CONDITION;
            case NONE -> ReasonStatus.MET;
        };
        lines.add(ConditionField.BREED_RULE, status, rule.text());
    }

    // 이동장 필요 — 이동장이나 유모차가 있으면 조건부, 둘 다 없으면 불가 · 비어 있으면 막지 않음
    private static void carrier(Conditions conditions, PetProfile pet, Lines lines) {
        if (!Boolean.TRUE.equals(conditions.carrierRequired())) {
            return;
        }
        if (pet.hasCarrier()) {
            lines.add(ConditionField.CARRIER_REQUIRED, ReasonStatus.CONDITION, "필요 — 이동장 있음");
        } else if (pet.hasStroller()) {
            lines.add(ConditionField.CARRIER_REQUIRED, ReasonStatus.CONDITION, "필요 — 유모차 있음");
        } else {
            lines.add(ConditionField.CARRIER_REQUIRED, ReasonStatus.NOT_MET, "필요 — 이동장 또는 유모차가 있어야 함");
        }
    }

    // 접종 증명 필요 — 증명서가 있으면 조건부, 없으면 불가 · 비어 있으면 막지 않음
    private static void vaccine(Conditions conditions, PetProfile pet, Lines lines) {
        if (!Boolean.TRUE.equals(conditions.vaccineProof())) {
            return;
        }
        if (pet.vaccineProofAvailable()) {
            lines.add(ConditionField.VACCINE_PROOF, ReasonStatus.CONDITION, "필요 — 접종 증명서 지참");
        } else {
            lines.add(ConditionField.VACCINE_PROOF, ReasonStatus.NOT_MET, "필요 — 접종 증명서가 있어야 함");
        }
    }

    // 장소가 거는 제한 — 어디서 · 언제 · 가기 전에 할 일
    private static void zonesAndDays(Conditions conditions, Lines lines) {
        if (hasItems(conditions.allowedZonesOnly())) {
            lines.add(ConditionField.ALLOWED_ZONES_ONLY, ReasonStatus.CONDITION,
                    joined(conditions.allowedZonesOnly()) + "에서만 동반 가능");
        }
        if (hasItems(conditions.excludedZones())) {
            lines.add(ConditionField.EXCLUDED_ZONES, ReasonStatus.CONDITION,
                    joined(conditions.excludedZones()) + " 동반 불가");
        }
        if (hasItems(conditions.excludedDays())) {
            lines.add(ConditionField.EXCLUDED_DAYS, ReasonStatus.CONDITION,
                    joined(conditions.excludedDays()) + " 동반 불가");
        }
        if (Boolean.TRUE.equals(conditions.advanceInquiry())) {
            lines.add(ConditionField.ADVANCE_INQUIRY, ReasonStatus.CONDITION, "방문 전 문의 필요");
        }
    }

    // 참고 줄 — 판정에 영향 없이 보이기만 함 · 요금은 금액이 있을 때만
    private static void notes(Conditions conditions, Lines lines) {
        if (Boolean.TRUE.equals(conditions.petOnly())) {
            lines.add(ConditionField.PET_ONLY, ReasonStatus.INFO, "반려견과 함께만 입장");
        }
        if (conditions.maxCount() != null) {
            lines.add(ConditionField.MAX_COUNT, ReasonStatus.INFO, "한 번에 " + conditions.maxCount() + "마리까지");
        }
        if (Boolean.TRUE.equals(conditions.leashRequired())) {
            lines.add(ConditionField.LEASH_REQUIRED, ReasonStatus.INFO, "필요");
        }
        Integer fee = conditions.extraFeeAmount();
        if (fee != null && fee > 0) {
            String unit = conditions.extraFeeUnit() == null ? "" : " · " + conditions.extraFeeUnit().text();
            lines.add(ConditionField.EXTRA_FEE_AMOUNT, ReasonStatus.INFO,
                    String.format(Locale.KOREA, "%,d원", fee) + unit, ConditionField.EXTRA_FEE_UNIT);
        }
        if (hasItems(conditions.requiredItems())) {
            lines.add(ConditionField.REQUIRED_ITEMS, ReasonStatus.INFO, joined(conditions.requiredItems()));
        }
    }

    // 반려견 크기와 체중 — "중형견(12kg)"
    private static String petSize(PetProfile pet) {
        String size = pet.breedSize().text();
        return pet.weightKg() == null ? size : size + "(" + kg(pet.weightKg()) + ")";
    }

    // 10.00 과 10 이 같은 글자가 되게 끝의 0 을 뗌
    private static String kg(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString() + "kg";
    }

    private static boolean hasItems(List<String> values) {
        return values != null && values.stream().anyMatch(value -> value != null && !value.isBlank());
    }

    private static String joined(List<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::strip)
                .collect(Collectors.joining(", "));
    }

    /**
     * 이유 줄을 모읍니다. 줄을 더할 때 그 칸의 근거를 붙입니다.
     */
    private static final class Lines {

        private final Map<String, List<EvidenceLine>> evidenceByField = new HashMap<>();
        private final List<Reason> reasons = new ArrayList<>();

        Lines(List<EvidenceLine> evidence) {
            for (EvidenceLine line : evidence) {
                evidenceByField.computeIfAbsent(line.fieldName(), name -> new ArrayList<>()).add(line);
            }
        }

        /**
         * @param also 이 줄에 함께 보일 칸 — 체중 제한에는 체중 기준, 추가 요금에는 요금 기준
         */
        void add(ConditionField field, ReasonStatus status, String message, ConditionField... also) {
            List<EvidenceLine> evidence = new ArrayList<>();
            attach(evidence, field);
            for (ConditionField other : also) {
                attach(evidence, other);
            }
            reasons.add(new Reason(field, field.label(), status, message, List.copyOf(evidence)));
        }

        void addMissingPet() {
            reasons.add(new Reason(null, "반려동물", ReasonStatus.MISSING, "반려동물 정보를 찾을 수 없음", List.of()));
        }

        // 같은 문장은 한 번만 — 한 문장이 두 칸의 근거인 경우가 흔함 ("10kg 이하" 가 체중 제한 · 체중 기준 둘 다)
        private void attach(List<EvidenceLine> into, ConditionField field) {
            for (EvidenceLine line : evidenceByField.getOrDefault(field.fieldName(), List.of())) {
                if (into.stream().noneMatch(line::sameSentence)) {
                    into.add(line);
                }
            }
        }

        /**
         * 막힌 이유부터 늘어놓습니다. 결과가 같으면 칸 순서이고, 칸이 없는 줄(반려동물)이 앞입니다.
         */
        List<Reason> sorted() {
            List<Reason> copy = new ArrayList<>(reasons);
            copy.sort(Comparator.comparing(Reason::status)
                    .thenComparingInt(reason -> reason.field() == null ? -1 : reason.field().ordinal()));
            return List.copyOf(copy);
        }
    }
}
