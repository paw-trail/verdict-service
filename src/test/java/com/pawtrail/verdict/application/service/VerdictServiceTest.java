package com.pawtrail.verdict.application.service;

import com.pawtrail.verdict.application.dto.output.PetVerdictDetail;
import com.pawtrail.verdict.application.dto.output.PetVerdictValue;
import com.pawtrail.verdict.application.dto.output.PlaceVerdictOutput;
import com.pawtrail.verdict.application.dto.output.PlaceVerdictSummary;
import com.pawtrail.verdict.application.dto.output.ReasonOutput;
import com.pawtrail.verdict.application.dto.output.VerdictBatchOutput;
import com.pawtrail.verdict.domain.enums.BreedSize;
import com.pawtrail.verdict.domain.enums.ReasonStatus;
import com.pawtrail.verdict.domain.enums.Scope;
import com.pawtrail.verdict.domain.enums.SizeRule;
import com.pawtrail.verdict.domain.enums.Verdict;
import com.pawtrail.verdict.domain.model.Conditions;
import com.pawtrail.verdict.domain.model.EvidenceLine;
import com.pawtrail.verdict.domain.model.PetProfile;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import com.pawtrail.verdict.domain.provider.PetProvider;
import com.pawtrail.verdict.domain.provider.PolicyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 목록 판정과 상세 판정의 조립을 봅니다. pet · policy 는 가짜로 바꿔 끼웁니다.
 *
 * 판정 규칙 자체는 VerdictJudgeTest 가 보고, 여기서는 순서 · 거르기 · 부르는 횟수 · 장소 칸을 봅니다.
 */
class VerdictServiceTest {

    private static final UUID PLACE = UUID.fromString("01a09015-b6bc-7812-8e7e-d0c59c46b007");
    private static final UUID NO_ROW = UUID.fromString("01a09015-0000-7000-8000-000000000002");
    private static final UUID SMALL = UUID.fromString("0199f000-0000-7000-8000-000000000001");
    private static final UUID MEDIUM = UUID.fromString("0199f000-0000-7000-8000-000000000002");
    private static final UUID GONE = UUID.fromString("0199f000-0000-7000-8000-000000000009");

    private static final Map<UUID, PetProfile> PETS = Map.of(
            SMALL, new PetProfile(SMALL, new BigDecimal("5"), BreedSize.SMALL, false, false, false, false),
            MEDIUM, new PetProfile(MEDIUM, new BigDecimal("12"), BreedSize.MEDIUM, false, false, false, false));

    @Test
    @DisplayName("요청한 장소를 요청 순서대로 모두 담고 조건 행이 없는 장소는 확인 필요")
    void 요청_순서대로_모두_담음() {
        VerdictService service = new VerdictService(ids -> PETS, ids -> Map.of(PLACE, smallOnly()));

        VerdictBatchOutput output = service.judgeBatch(List.of(NO_ROW, PLACE), List.of(SMALL));

        assertThat(output.results()).extracting(PlaceVerdictSummary::placeId).containsExactly(NO_ROW, PLACE);
        assertThat(output.results().get(0).verdicts()).extracting(PetVerdictValue::verdict).containsExactly(Verdict.UNKNOWN);
        assertThat(output.results().get(1).verdicts()).extracting(PetVerdictValue::verdict).containsExactly(Verdict.ALLOWED);
    }

    @Test
    @DisplayName("중복과 null 은 걸러 요청 순서를 지키고 두 서비스에 한 번씩만 물음")
    void 중복과_null_을_거름() {
        List<List<UUID>> petCalls = new ArrayList<>();
        List<List<UUID>> policyCalls = new ArrayList<>();
        PetProvider pets = ids -> {
            petCalls.add(ids);
            return PETS;
        };
        PolicyProvider policies = ids -> {
            policyCalls.add(ids);
            return Map.of(PLACE, smallOnly());
        };

        VerdictBatchOutput output = new VerdictService(pets, policies)
                .judgeBatch(Arrays.asList(PLACE, null, PLACE), Arrays.asList(MEDIUM, SMALL, MEDIUM, null));

        assertThat(output.results()).hasSize(1);
        assertThat(output.results().get(0).verdicts()).extracting(PetVerdictValue::petId).containsExactly(MEDIUM, SMALL);
        assertThat(petCalls).containsExactly(List.of(MEDIUM, SMALL));
        assertThat(policyCalls).containsExactly(List.of(PLACE));
    }

    @Test
    @DisplayName("받은 반려동물이 없으면 policy 를 부르지 않고 모두 확인 필요")
    void 반려동물이_없으면_policy_를_안_부름() {
        PolicyProvider mustNotCall = ids -> {
            throw new AssertionError("policy 를 부르면 안 됨");
        };

        VerdictBatchOutput output = new VerdictService(ids -> Map.of(), mustNotCall)
                .judgeBatch(List.of(PLACE), List.of(GONE));

        assertThat(output.results().get(0).verdicts()).extracting(PetVerdictValue::verdict).containsExactly(Verdict.UNKNOWN);
        assertThat(output.results().get(0).requiredItems()).isEmpty();
    }

    @Test
    @DisplayName("장소 칸 — 충돌 여부 · 가장 제한이 센 마리의 한 줄 근거 · 준비물")
    void 장소_칸이_붙음() {
        PlaceConditions place = new PlaceConditions(PLACE,
                Conditions.builder().scope(Scope.ALL_AREA).sizeRule(SizeRule.SMALL_ONLY)
                        .leashRequired(true).requiredItems(List.of("배변봉투")).build(),
                true, null,
                List.of(new EvidenceLine("sizeRule", "GOCAMPING", "intro", "소형견만 출입 허용", "LLM")));

        PlaceVerdictSummary summary = new VerdictService(ids -> PETS, ids -> Map.of(PLACE, place))
                .judgeBatch(List.of(PLACE), List.of(SMALL, MEDIUM)).results().get(0);

        assertThat(summary.hasConflict()).isEqualTo(true);
        assertThat(summary.verdicts()).extracting(PetVerdictValue::verdict)
                .containsExactly(Verdict.ALLOWED, Verdict.NOT_ALLOWED);
        assertThat(summary.evidenceSummary()).isEqualTo("크기 제한: 소형견만 출입 허용");
        assertThat(summary.requiredItems()).containsExactly("목줄", "배변봉투");
    }

    @Test
    @DisplayName("상세 판정은 마리마다 이유 줄을 담고 정정 출처를 장소 칸에 둠")
    void 상세_판정() {
        PlaceConditions corrected = new PlaceConditions(PLACE,
                Conditions.builder().scope(Scope.ALL_AREA).sizeRule(SizeRule.SMALL_ONLY).build(),
                false, "MANUAL", List.of());

        PlaceVerdictOutput output = new VerdictService(ids -> PETS, ids -> Map.of(PLACE, corrected))
                .judgePlace(PLACE, List.of(MEDIUM, GONE));

        assertThat(output.correctionSource()).isEqualTo("MANUAL");
        assertThat(output.verdicts()).extracting(PetVerdictDetail::verdict)
                .containsExactly(Verdict.NOT_ALLOWED, Verdict.UNKNOWN);
        List<ReasonOutput> medium = output.verdicts().get(0).reasons();
        assertThat(medium).extracting(ReasonOutput::field).containsExactly("sizeRule", "scope");
        assertThat(medium).extracting(ReasonOutput::label).containsExactly("크기 제한", "동반 범위");
        assertThat(medium).extracting(ReasonOutput::status).containsExactly(ReasonStatus.NOT_MET, ReasonStatus.MET);
        assertThat(output.verdicts().get(1).reasons()).extracting(ReasonOutput::label).containsExactly("반려동물");
    }

    private static PlaceConditions smallOnly() {
        return new PlaceConditions(PLACE,
                Conditions.builder().scope(Scope.ALL_AREA).sizeRule(SizeRule.SMALL_ONLY).build(),
                false, null, List.of());
    }
}
