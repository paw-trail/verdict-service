package com.pawtrail.verdict.domain.rule;

import com.pawtrail.verdict.domain.enums.BreedSize;
import com.pawtrail.verdict.domain.enums.Scope;
import com.pawtrail.verdict.domain.enums.SizeRule;
import com.pawtrail.verdict.domain.model.Conditions;
import com.pawtrail.verdict.domain.model.EvidenceLine;
import com.pawtrail.verdict.domain.model.PetJudgement;
import com.pawtrail.verdict.domain.model.PetProfile;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 목록 카드의 한 줄 근거를 봅니다. 사용자 서비스는 이 값을 읽지 않고 search 카드가 씁니다.
 */
class CardSummaryTest {

    private static final UUID PLACE_ID = UUID.fromString("01a09015-b6bc-7812-8e7e-d0c59c46b007");
    private static final UUID SMALL_ID = UUID.fromString("0199f000-0000-7000-8000-000000000001");
    private static final UUID MEDIUM_ID = UUID.fromString("0199f000-0000-7000-8000-000000000002");

    @Test
    @DisplayName("여러 마리면 제한이 가장 센 마리 기준 · 칸 이름을 붙인 그 줄의 원문 근거")
    void 가장_제한이_센_마리() {
        PlaceConditions place = new PlaceConditions(PLACE_ID,
                Conditions.builder().scope(Scope.ALL_AREA).sizeRule(SizeRule.SMALL_ONLY).build(), false, null,
                List.of(new EvidenceLine("sizeRule", "GOCAMPING", "intro", "소형견만 출입 허용", "LLM")));

        PetJudgement small = VerdictJudge.judge(SMALL_ID, pet(SMALL_ID, "5", BreedSize.SMALL, false, false), place);
        PetJudgement medium = VerdictJudge.judge(MEDIUM_ID, pet(MEDIUM_ID, "12", BreedSize.MEDIUM, false, false), place);

        assertThat(CardSummary.of(List.of(small, medium))).isEqualTo("크기 제한: 소형견만 출입 허용");
    }

    @Test
    @DisplayName("근거가 없으면 원문 자리에 그 줄의 문장 — 관리자 정정이 이긴 장소 · 비어 있는 칸")
    void 근거가_없으면_문장() {
        PlaceConditions place = new PlaceConditions(PLACE_ID,
                Conditions.builder().scope(Scope.ALL_AREA).build(), false, "MANUAL", List.of());

        PetJudgement judgement = VerdictJudge.judge(SMALL_ID, pet(SMALL_ID, "5", BreedSize.SMALL, false, false), place);

        assertThat(CardSummary.of(List.of(judgement))).isEqualTo("크기 제한: 크기 · 체중 조건 정보 없음");
    }

    @Test
    @DisplayName("같은 단계면 요청 순서가 앞선 마리")
    void 같은_단계면_요청_순서() {
        PlaceConditions place = new PlaceConditions(PLACE_ID,
                Conditions.builder().scope(Scope.ALL_AREA).sizeRule(SizeRule.ALL).carrierRequired(true).build(),
                false, null, List.of());

        PetJudgement carrier = VerdictJudge.judge(SMALL_ID, pet(SMALL_ID, "5", BreedSize.SMALL, true, false), place);
        PetJudgement stroller = VerdictJudge.judge(MEDIUM_ID, pet(MEDIUM_ID, "5", BreedSize.SMALL, false, true), place);

        assertThat(CardSummary.of(List.of(carrier, stroller))).isEqualTo("이동장: 필요 — 이동장 있음");
        assertThat(CardSummary.of(List.of(stroller, carrier))).isEqualTo("이동장: 필요 — 유모차 있음");
    }

    @Test
    @DisplayName("판정한 마리가 없으면 null")
    void 빈_목록이면_null() {
        assertThat(CardSummary.of(List.of())).isNull();
    }

    private static PetProfile pet(UUID id, String kg, BreedSize size, boolean carrier, boolean stroller) {
        return new PetProfile(id, new BigDecimal(kg), size, carrier, stroller, false, false);
    }
}
