package com.pawtrail.verdict.application.dto.output;

import com.pawtrail.verdict.domain.enums.ReasonStatus;
import com.pawtrail.verdict.domain.model.Reason;

import java.util.List;

/**
 * 이유 줄 하나입니다.
 *
 * @param field    칸 이름(camelCase) — 반려동물 정보를 못 찾은 줄은 null
 * @param label    화면에 보이는 칸 이름표
 * @param status   NOT_MET · MISSING · CONDITION · MET · INFO — 화면은 이 값으로 아이콘을 고름
 * @param message  반려견 값과 조건 값을 함께 담은 문장
 * @param evidence 근거 문장들 — 출처 말은 화면이 추출 방식으로 만듦
 */
public record ReasonOutput(
        String field,
        String label,
        ReasonStatus status,
        String message,
        List<EvidenceOutput> evidence
) {

    public static ReasonOutput from(Reason reason) {
        return new ReasonOutput(
                reason.field() == null ? null : reason.field().fieldName(),
                reason.label(),
                reason.status(),
                reason.message(),
                reason.evidence().stream().map(EvidenceOutput::from).toList());
    }
}
