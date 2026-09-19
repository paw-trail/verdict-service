package com.pawtrail.verdict.application.dto.output;

import com.pawtrail.verdict.domain.model.PetJudgement;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import com.pawtrail.verdict.domain.rule.CardSummary;
import com.pawtrail.verdict.domain.rule.RequiredItems;

import java.util.List;
import java.util.UUID;

/**
 * 목록 판정의 장소 하나입니다. 이유 줄은 싣지 않습니다(응답 부피).
 *
 * @param placeId         장소 식별자
 * @param hasConflict     조건이 갈린 장소인지 — 판정과 따로 배지로 알림
 * @param verdicts        마리별 판정 — 요청 순서 그대로
 * @param evidenceSummary 카드 한 줄 근거 — 제한이 가장 센 마리 기준 · "칸 이름: 원문 근거"
 * @param requiredItems   준비물 — 장소마다 하나 · 비어 있을 수 있으나 null 은 아님
 */
public record PlaceVerdictSummary(
        UUID placeId,
        boolean hasConflict,
        List<PetVerdictValue> verdicts,
        String evidenceSummary,
        List<String> requiredItems
) {

    /**
     * @param place 조건 행이 없는 장소면 null
     */
    public static PlaceVerdictSummary of(UUID placeId, PlaceConditions place, List<PetJudgement> judgements) {
        return new PlaceVerdictSummary(
                placeId,
                place != null && place.hasConflict(),
                judgements.stream().map(PetVerdictValue::from).toList(),
                CardSummary.of(judgements),
                RequiredItems.of(place == null ? null : place.conditions()));
    }
}
