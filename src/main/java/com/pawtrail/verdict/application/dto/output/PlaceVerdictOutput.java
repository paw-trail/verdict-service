package com.pawtrail.verdict.application.dto.output;

import com.pawtrail.verdict.domain.model.PetJudgement;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import com.pawtrail.verdict.domain.rule.RequiredItems;

import java.util.List;
import java.util.UUID;

/**
 * 장소 상세 판정의 응답입니다.
 *
 * 장소에 딸린 칸(충돌 여부 · 정정 출처 · 준비물)을 한 벌 두고, 마리마다 판정과 이유 줄을 둡니다.
 * 반려동물 요약과 규칙 판은 싣지 않습니다. 화면이 GET /pets 로 반려동물을 이미 가지고, 규칙 판은 캐시가 생길 때 되살립니다.
 *
 * @param correctionSource 관리자 정정이 이긴 장소면 MANUAL · OWNER — 화면이 "관리자 확인" 으로 표시 · 아니면 null
 */
public record PlaceVerdictOutput(
        UUID placeId,
        boolean hasConflict,
        String correctionSource,
        List<String> requiredItems,
        List<PetVerdictDetail> verdicts
) {

    /**
     * @param place 조건 행이 없는 장소면 null
     */
    public static PlaceVerdictOutput of(UUID placeId, PlaceConditions place, List<PetJudgement> judgements) {
        return new PlaceVerdictOutput(
                placeId,
                place != null && place.hasConflict(),
                place == null ? null : place.correctionSource(),
                RequiredItems.of(place == null ? null : place.conditions()),
                judgements.stream().map(PetVerdictDetail::from).toList());
    }
}
