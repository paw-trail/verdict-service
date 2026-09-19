package com.pawtrail.verdict.domain.provider;

import com.pawtrail.verdict.domain.model.PetProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 반려동물 정보를 받아 오는 약속입니다. 구현은 pet 을 부릅니다.
 */
public interface PetProvider {

    /**
     * 반려동물 정보를 한 번에 받습니다.
     *
     * 없거나 부른 사람의 것이 아닌 반려동물은 결과에 없습니다. pet 이 둘을 가르지 않고 조용히 빼기 때문이며,
     * 판정은 빠진 반려동물을 "확인 필요" 로 냅니다.
     * 부르지 못하면 PET_UNAVAILABLE 을 던집니다.
     *
     * @param petIds 100개까지 — pet 의 상한과 같음 · 비어 있으면 부르지 않음
     * @return 반려동물 식별자로 찾는 표
     */
    Map<UUID, PetProfile> findByIds(List<UUID> petIds);
}
