package com.pawtrail.verdict.application.dto.output;

import com.pawtrail.verdict.domain.enums.Verdict;
import com.pawtrail.verdict.domain.model.PetJudgement;

import java.util.UUID;

/**
 * 목록 판정의 마리 하나 — 판정 값만 담습니다.
 */
public record PetVerdictValue(UUID petId, Verdict verdict) {

    public static PetVerdictValue from(PetJudgement judgement) {
        return new PetVerdictValue(judgement.petId(), judgement.verdict());
    }
}
