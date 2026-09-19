package com.pawtrail.verdict.domain.provider;

import com.pawtrail.verdict.domain.model.PlaceConditions;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 장소의 동반 조건을 받아 오는 약속입니다. 구현은 policy 를 부릅니다.
 */
public interface PolicyProvider {

    /**
     * 장소들의 동반 조건을 한 번에 받습니다.
     *
     * 조건 행이 없는 장소는 결과에 없습니다. 판정은 그 장소를 "확인 필요" 로 냅니다.
     * 부르지 못하면 POLICY_UNAVAILABLE 을 던집니다.
     *
     * @param placeIds 500곳까지 — policy 의 상한과 같음 · 비어 있으면 부르지 않음
     * @return 장소 식별자로 찾는 표
     */
    Map<UUID, PlaceConditions> findByPlaceIds(List<UUID> placeIds);
}
