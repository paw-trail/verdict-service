package com.pawtrail.verdict.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * 한 장소에 대해 policy 에서 받은 것입니다. 조건 행이 없는 장소는 이 값이 아예 없습니다.
 *
 * @param placeId          장소 식별자
 * @param conditions       조건 20칸
 * @param hasConflict      출처끼리나 한 출처 안에서 조건이 갈렸는지 — 판정과 따로 배지로 알림
 * @param correctionSource 관리자 정정이 이긴 장소면 MANUAL · OWNER, 아니면 null
 * @param evidence         칸마다 그 값을 만든 소스의 근거
 */
public record PlaceConditions(
        UUID placeId,
        Conditions conditions,
        boolean hasConflict,
        String correctionSource,
        List<EvidenceLine> evidence
) {
}
