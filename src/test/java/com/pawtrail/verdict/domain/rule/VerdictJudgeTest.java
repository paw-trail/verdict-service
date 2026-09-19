package com.pawtrail.verdict.domain.rule;

import com.pawtrail.verdict.domain.enums.BreedRule;
import com.pawtrail.verdict.domain.enums.BreedSize;
import com.pawtrail.verdict.domain.enums.ConditionField;
import com.pawtrail.verdict.domain.enums.ExtraFeeUnit;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 판정 네 단계와 이유 줄을 봅니다.
 *
 * 규칙 표는 착수 때 정한 그대로입니다 — 불가 · 확인 필요 · 조건부 · 가능, 앞선 것이 이김.
 * 기본 장소는 막힘이 없는 곳(전 구역 · 크기 제한 없음)이고 검사마다 한두 칸만 바꿉니다.
 */
class VerdictJudgeTest {

    private static final UUID PLACE_ID = UUID.fromString("01a09015-b6bc-7812-8e7e-d0c59c46b007");
    private static final UUID PET_ID = UUID.fromString("0199f000-0000-7000-8000-000000000001");

    @Test
    @DisplayName("조건 행이 없는 장소는 확인 필요 · 줄 하나")
    void 조건_행이_없으면_확인_필요() {
        PetJudgement judgement = VerdictJudge.judge(PET_ID, dog("5", BreedSize.SMALL), null);

        assertThat(judgement.verdict()).isEqualTo(Verdict.UNKNOWN);
        assertThat(judgement.reasons()).extracting(Reason::message).containsExactly("동반 조건 정보 없음");
    }

    @Test
    @DisplayName("없거나 남의 반려동물은 확인 필요 · 칸 없는 줄 하나")
    void 반려동물이_없으면_확인_필요() {
        PetJudgement judgement = VerdictJudge.judge(PET_ID, null, place(open().build()));

        assertThat(judgement.petId()).isEqualTo(PET_ID);
        assertThat(judgement.verdict()).isEqualTo(Verdict.UNKNOWN);
        assertThat(judgement.reasons()).extracting(Reason::label).containsExactly("반려동물");
        assertThat(judgement.reasons()).extracting(Reason::field).containsExactly((ConditionField) null);
    }

    @Test
    @DisplayName("범위가 동반 불가면 불가 — 실내 · 실외 줄은 두지 않음")
    void 범위가_동반_불가면_불가() {
        Conditions conditions = open().scope(Scope.NONE).indoorAllowed(false).outdoorAllowed(false).build();

        PetJudgement judgement = judge(conditions);

        assertThat(judgement.verdict()).isEqualTo(Verdict.NOT_ALLOWED);
        assertThat(judgement.reasons()).extracting(Reason::field)
                .containsExactly(ConditionField.SCOPE, ConditionField.SIZE_RULE);
    }

    @Test
    @DisplayName("실내 · 실외가 둘 다 불가면 불가")
    void 실내_실외_둘_다_불가면_불가() {
        Conditions conditions = open().scope(null).indoorAllowed(false).outdoorAllowed(false).build();

        PetJudgement judgement = judge(conditions);

        assertThat(judgement.verdict()).isEqualTo(Verdict.NOT_ALLOWED);
        assertThat(judgement.reasons()).extracting(Reason::status)
                .containsExactly(ReasonStatus.NOT_MET, ReasonStatus.NOT_MET, ReasonStatus.MET);
    }

    @Test
    @DisplayName("어디서 된다는 말이 없으면 동반 자체를 모름 — 실내만 불가라고 적혀 있어도")
    void 동반_자체를_모르면_확인_필요() {
        Conditions conditions = open().scope(null).indoorAllowed(false).build();

        PetJudgement judgement = judge(conditions);

        assertThat(judgement.verdict()).isEqualTo(Verdict.UNKNOWN);
        assertThat(judgement.reasons()).extracting(Reason::message)
                .containsExactly("동반 가능 여부 정보 없음", "불가", "제한 없음");
    }

    @Test
    @DisplayName("실외만 되는 곳은 조건부 — 안 되는 쪽 줄에 되는 쪽을 밝힘")
    void 실외만_되면_조건부() {
        Conditions conditions = open().scope(null).indoorAllowed(false).outdoorAllowed(true).build();

        PetJudgement judgement = judge(conditions);

        assertThat(judgement.verdict()).isEqualTo(Verdict.CONDITIONAL);
        assertThat(judgement.reasons()).extracting(Reason::message)
                .containsExactly("불가 — 실외에서만 동반 가능", "가능", "제한 없음");
    }

    @Test
    @DisplayName("일부 구역은 조건부")
    void 일부_구역이면_조건부() {
        PetJudgement judgement = judge(open().scope(Scope.PARTIAL).build());

        assertThat(judgement.verdict()).isEqualTo(Verdict.CONDITIONAL);
    }

    @Test
    @DisplayName("크기 · 체중이 둘 다 비면 확인 필요")
    void 크기_체중이_둘_다_비면_확인_필요() {
        PetJudgement judgement = judge(open().sizeRule(null).build());

        assertThat(judgement.verdict()).isEqualTo(Verdict.UNKNOWN);
        assertThat(judgement.reasons()).extracting(Reason::message)
                .containsExactly("크기 · 체중 조건 정보 없음", "전 구역 동반 가능");
    }

    @Test
    @DisplayName("소형견만 받는 곳에 중형견이면 불가 — 반려견 값과 조건 값을 함께 적음")
    void 소형견만인데_중형견이면_불가() {
        Conditions conditions = open().sizeRule(SizeRule.SMALL_ONLY).build();

        PetJudgement medium = VerdictJudge.judge(PET_ID, dog("12", BreedSize.MEDIUM), place(conditions));
        PetJudgement small = VerdictJudge.judge(PET_ID, dog("5.40", BreedSize.SMALL), place(conditions));

        assertThat(medium.verdict()).isEqualTo(Verdict.NOT_ALLOWED);
        assertThat(medium.reasons()).extracting(Reason::message).containsExactly("중형견(12kg) — 소형견만", "전 구역 동반 가능");
        assertThat(small.verdict()).isEqualTo(Verdict.ALLOWED);
        assertThat(small.reasons()).extracting(Reason::message).containsExactly("전 구역 동반 가능", "소형견(5.4kg) — 소형견만");
    }

    @Test
    @DisplayName("체중이 상한과 같으면 이하 · 미만으로 가르고, 모르면 확인 필요")
    void 체중_경계() {
        PetProfile tenKg = dog("10", BreedSize.MEDIUM);

        assertThat(VerdictJudge.judge(PET_ID, tenKg, place(weight("10.00", true))).verdict()).isEqualTo(Verdict.ALLOWED);
        assertThat(VerdictJudge.judge(PET_ID, tenKg, place(weight("10", false))).verdict()).isEqualTo(Verdict.NOT_ALLOWED);
        PetJudgement unknown = VerdictJudge.judge(PET_ID, tenKg, place(weight("10", null)));
        assertThat(unknown.verdict()).isEqualTo(Verdict.UNKNOWN);
        assertThat(unknown.reasons()).extracting(Reason::message)
                .containsExactly("10kg — 10kg (이하인지 미만인지 정보 없음)", "전 구역 동반 가능");
    }

    @Test
    @DisplayName("맹견 규칙은 맹견에만 걸림 — 불가 · 입마개 조건부 · 비면 확인 필요")
    void 맹견_규칙() {
        PetProfile dangerous = new PetProfile(PET_ID, new BigDecimal("30"), BreedSize.LARGE, false, false, false, true);

        assertThat(VerdictJudge.judge(PET_ID, dangerous, place(open().breedRule(BreedRule.DANGEROUS_BANNED).build())).verdict())
                .isEqualTo(Verdict.NOT_ALLOWED);
        assertThat(VerdictJudge.judge(PET_ID, dangerous, place(open().breedRule(BreedRule.DANGEROUS_MUZZLE).build())).verdict())
                .isEqualTo(Verdict.CONDITIONAL);
        assertThat(VerdictJudge.judge(PET_ID, dangerous, place(open().build())).verdict())
                .isEqualTo(Verdict.UNKNOWN);
        assertThat(judge(open().breedRule(BreedRule.DANGEROUS_BANNED).build()).verdict())
                .isEqualTo(Verdict.ALLOWED);
    }

    @Test
    @DisplayName("맹견인지 모르면 제한 없음이 아닌 한 확인 필요 — 맹견 아님으로 넘기지 않음")
    void 맹견인지_모르면_확인_필요() {
        PetProfile unknown = new PetProfile(PET_ID, new BigDecimal("30"), BreedSize.LARGE, false, false, false, null);

        PetJudgement banned = VerdictJudge.judge(PET_ID, unknown, place(open().breedRule(BreedRule.DANGEROUS_BANNED).build()));

        assertThat(banned.verdict()).isEqualTo(Verdict.UNKNOWN);
        assertThat(banned.reasons()).extracting(Reason::message)
                .containsExactly("반려견의 맹견 여부 정보 없음 — 맹견 불가", "전 구역 동반 가능", "제한 없음");
        assertThat(VerdictJudge.judge(PET_ID, unknown, place(open().build())).verdict())
                .isEqualTo(Verdict.UNKNOWN);
        assertThat(VerdictJudge.judge(PET_ID, unknown, place(open().breedRule(BreedRule.NONE).build())).verdict())
                .isEqualTo(Verdict.ALLOWED);
    }

    @Test
    @DisplayName("이동장 필요 — 이동장이나 유모차가 있으면 조건부, 없으면 불가")
    void 이동장_필요() {
        PlaceConditions place = place(open().carrierRequired(true).build());

        PetJudgement carrier = VerdictJudge.judge(PET_ID, new PetProfile(PET_ID, new BigDecimal("4"), BreedSize.SMALL, true, false, false, false), place);
        PetJudgement stroller = VerdictJudge.judge(PET_ID, new PetProfile(PET_ID, new BigDecimal("4"), BreedSize.SMALL, false, true, false, false), place);
        PetJudgement none = VerdictJudge.judge(PET_ID, dog("4", BreedSize.SMALL), place);

        assertThat(carrier.verdict()).isEqualTo(Verdict.CONDITIONAL);
        assertThat(carrier.reasons()).extracting(Reason::message).containsExactly("필요 — 이동장 있음", "전 구역 동반 가능", "제한 없음");
        assertThat(stroller.reasons()).extracting(Reason::message).containsExactly("필요 — 유모차 있음", "전 구역 동반 가능", "제한 없음");
        assertThat(none.verdict()).isEqualTo(Verdict.NOT_ALLOWED);
    }

    @Test
    @DisplayName("이동장 · 접종 증명이 비어 있으면 막지 않음")
    void 이동장_접종이_비면_막지_않음() {
        assertThat(judge(open().build()).verdict()).isEqualTo(Verdict.ALLOWED);
    }

    @Test
    @DisplayName("접종 증명 필요 — 증명서가 있으면 조건부, 없으면 불가")
    void 접종_증명_필요() {
        PlaceConditions place = place(open().vaccineProof(true).build());

        PetJudgement proof = VerdictJudge.judge(PET_ID, new PetProfile(PET_ID, new BigDecimal("4"), BreedSize.SMALL, false, false, true, false), place);

        assertThat(proof.verdict()).isEqualTo(Verdict.CONDITIONAL);
        assertThat(VerdictJudge.judge(PET_ID, dog("4", BreedSize.SMALL), place).verdict()).isEqualTo(Verdict.NOT_ALLOWED);
    }

    @Test
    @DisplayName("허용 구역 · 제외 구역 · 제외 요일 · 사전 문의는 조건부")
    void 장소가_거는_제한은_조건부() {
        Conditions conditions = open()
                .allowedZonesOnly(List.of("반려 구역"))
                .excludedZones(List.of("수영장", "객실"))
                .excludedDays(List.of("월요일"))
                .advanceInquiry(true)
                .build();

        PetJudgement judgement = judge(conditions);

        assertThat(judgement.verdict()).isEqualTo(Verdict.CONDITIONAL);
        assertThat(judgement.reasons()).extracting(Reason::message).containsExactly(
                "수영장, 객실 동반 불가", "반려 구역에서만 동반 가능", "월요일 동반 불가", "방문 전 문의 필요",
                "전 구역 동반 가능", "제한 없음");
    }

    @Test
    @DisplayName("반려견 동반 전용 · 마릿수 · 목줄 · 추가 요금 · 준비물은 참고 줄 — 판정을 바꾸지 않음")
    void 참고_줄은_판정을_바꾸지_않음() {
        Conditions conditions = open()
                .petOnly(true)
                .maxCount(2)
                .leashRequired(true)
                .extraFeeAmount(10000)
                .extraFeeUnit(ExtraFeeUnit.PER_DOG)
                .requiredItems(List.of("배변봉투", " "))
                .build();

        PetJudgement judgement = judge(conditions);

        assertThat(judgement.verdict()).isEqualTo(Verdict.ALLOWED);
        assertThat(judgement.reasons()).extracting(Reason::message).containsExactly(
                "전 구역 동반 가능", "제한 없음",
                "반려견과 함께만 입장", "한 번에 2마리까지", "필요", "10,000원 · 마리당", "배변봉투");
    }

    @Test
    @DisplayName("추가 요금이 0원이면 줄을 두지 않음")
    void 무료면_요금_줄이_없음() {
        PetJudgement judgement = judge(open().extraFeeAmount(0).build());

        assertThat(judgement.reasons()).hasSize(2);
    }

    @Test
    @DisplayName("안내견 한정이면 불가")
    void 안내견_한정이면_불가() {
        assertThat(judge(open().guideDogOnly(true).build()).verdict()).isEqualTo(Verdict.NOT_ALLOWED);
    }

    @Test
    @DisplayName("줄은 막힌 이유부터 — 불가 · 확인 필요 · 조건부 · 충족 · 참고")
    void 줄은_막힌_이유부터() {
        PetProfile dangerous = new PetProfile(PET_ID, new BigDecimal("12"), BreedSize.MEDIUM, false, false, false, true);
        Conditions conditions = open()
                .sizeRule(SizeRule.SMALL_ONLY)
                .excludedZones(List.of("실내"))
                .leashRequired(true)
                .build();

        PetJudgement judgement = VerdictJudge.judge(PET_ID, dangerous, place(conditions));

        assertThat(judgement.reasons()).extracting(Reason::status).containsExactly(
                ReasonStatus.NOT_MET, ReasonStatus.MISSING, ReasonStatus.CONDITION, ReasonStatus.MET, ReasonStatus.INFO);
    }

    @Test
    @DisplayName("근거는 그 칸의 것을 붙이고 같은 문장은 한 번만 — 추출 방식을 그대로 실음")
    void 근거는_같은_문장을_한_번만() {
        Conditions conditions = open().maxWeightKg(new BigDecimal("10")).weightInclusive(true).build();
        PlaceConditions place = place(conditions,
                new EvidenceLine("maxWeightKg", "GOCAMPING", "intro", "10kg 이하 반려견만", "LLM"),
                new EvidenceLine("weightInclusive", "GOCAMPING", "intro", "10kg 이하 반려견만", "LLM"),
                new EvidenceLine("scope", "PET_TOUR", "acmpyTypeCd", "전구역 동반가능", "RULE"));

        PetJudgement judgement = VerdictJudge.judge(PET_ID, dog("8", BreedSize.SMALL), place);

        Reason weightLine = judgement.reasons().stream()
                .filter(reason -> reason.field() == ConditionField.MAX_WEIGHT_KG).findFirst().orElseThrow();
        assertThat(weightLine.message()).isEqualTo("8kg — 10kg 이하");
        assertThat(weightLine.evidence()).extracting(EvidenceLine::extractionMethod).containsExactly("LLM");
        Reason scopeLine = judgement.reasons().getFirst();
        assertThat(scopeLine.evidence()).extracting(EvidenceLine::text).containsExactly("전구역 동반가능");
    }

    private static PetJudgement judge(Conditions conditions) {
        return VerdictJudge.judge(PET_ID, dog("5", BreedSize.SMALL), place(conditions));
    }

    // 막힘이 없는 기본 장소 — 전 구역 · 크기 제한 없음
    private static Conditions.Builder open() {
        return Conditions.builder().scope(Scope.ALL_AREA).sizeRule(SizeRule.ALL);
    }

    private static Conditions weight(String limit, Boolean inclusive) {
        return Conditions.builder().scope(Scope.ALL_AREA)
                .maxWeightKg(new BigDecimal(limit)).weightInclusive(inclusive).build();
    }

    private static PetProfile dog(String kg, BreedSize size) {
        return new PetProfile(PET_ID, new BigDecimal(kg), size, false, false, false, false);
    }

    private static PlaceConditions place(Conditions conditions, EvidenceLine... evidence) {
        return new PlaceConditions(PLACE_ID, conditions, false, null, List.of(evidence));
    }
}
