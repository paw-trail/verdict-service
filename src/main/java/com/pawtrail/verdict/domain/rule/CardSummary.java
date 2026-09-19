package com.pawtrail.verdict.domain.rule;

import com.pawtrail.verdict.domain.enums.ReasonStatus;
import com.pawtrail.verdict.domain.model.PetJudgement;
import com.pawtrail.verdict.domain.model.Reason;

import java.util.List;

/**
 * 목록 카드에 붙는 한 줄 근거를 고릅니다.
 *
 * <b>줄을 고르는 규칙</b> — 판정을 정한 줄입니다.
 * 불가면 막은 줄, 확인 필요면 빈 칸의 줄, 조건부면 지켜야 할 줄, 가능이면 충족한 줄입니다.
 *
 * <b>여러 마리면 제한이 가장 센 마리 기준입니다.</b> 카드 한 줄은 "왜 이 배지인가" 를 말하는 자리라
 * 막히는 쪽을 보여 줍니다. 같은 단계면 요청 순서가 앞선 마리입니다. 나머지 마리의 사정은 상세에서 봅니다.
 *
 * <b>문구는 원문 근거입니다.</b> 그 줄의 첫 근거 문장을 그대로 쓰고,
 * 근거가 없으면(관리자 정정이 이긴 장소 · 조건 정보 없음) 그 줄의 문장을 씁니다.
 */
public final class CardSummary {

    private CardSummary() {
    }

    /**
     * @param judgements 한 장소의 마리별 판정 — 요청 순서 그대로
     * @return 한 줄 근거. 판정한 마리가 없으면 null
     */
    public static String of(List<PetJudgement> judgements) {
        PetJudgement strictest = null;
        for (PetJudgement judgement : judgements) {
            if (strictest == null || judgement.verdict().compareTo(strictest.verdict()) < 0) {
                strictest = judgement;
            }
        }
        if (strictest == null) {
            return null;
        }
        ReasonStatus deciding = switch (strictest.verdict()) {
            case NOT_ALLOWED -> ReasonStatus.NOT_MET;
            case UNKNOWN -> ReasonStatus.MISSING;
            case CONDITIONAL -> ReasonStatus.CONDITION;
            case ALLOWED -> ReasonStatus.MET;
        };
        for (Reason reason : strictest.reasons()) {
            if (reason.status() == deciding) {
                return reason.evidence().isEmpty() ? reason.message() : reason.evidence().getFirst().text();
            }
        }
        return null;
    }
}
