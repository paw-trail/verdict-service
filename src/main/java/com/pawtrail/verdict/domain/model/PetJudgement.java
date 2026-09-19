package com.pawtrail.verdict.domain.model;

import com.pawtrail.verdict.domain.enums.Verdict;

import java.util.List;
import java.util.UUID;

/**
 * 한 장소에서 반려동물 한 마리의 판정입니다.
 *
 * @param petId   반려동물 식별자
 * @param verdict 판정
 * @param reasons 이유 줄 — 막힌 이유부터 늘어놓음
 */
public record PetJudgement(
        UUID petId,
        Verdict verdict,
        List<Reason> reasons
) {
}
