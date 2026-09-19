package com.pawtrail.verdict.application.dto.output;

import com.pawtrail.verdict.domain.enums.Verdict;
import com.pawtrail.verdict.domain.model.PetJudgement;

import java.util.List;
import java.util.UUID;

/**
 * 장소 상세 판정의 마리 하나 — 판정과 이유 줄입니다.
 *
 * @param reasons 막힌 이유부터 늘어놓은 줄
 */
public record PetVerdictDetail(UUID petId, Verdict verdict, List<ReasonOutput> reasons) {

    public static PetVerdictDetail from(PetJudgement judgement) {
        return new PetVerdictDetail(judgement.petId(), judgement.verdict(),
                judgement.reasons().stream().map(ReasonOutput::from).toList());
    }
}
