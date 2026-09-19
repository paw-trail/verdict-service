package com.pawtrail.verdict.domain.model;

import com.pawtrail.verdict.domain.enums.ConditionField;
import com.pawtrail.verdict.domain.enums.ReasonStatus;

import java.util.List;

/**
 * 판정 이유 한 줄입니다.
 *
 * @param field    어느 칸의 줄인지. 반려동물 정보를 못 찾은 줄은 칸이 없어 null
 * @param label    화면에 보이는 칸 이름표
 * @param status   이 줄의 결과
 * @param message  이 서비스가 만든 문장 — 반려견 값과 조건 값을 함께 담음
 * @param evidence 그 칸의 근거 문장들. 출처 말(공공데이터 항목 · 안내문을 AI 가 읽음)은 화면이 추출 방식으로 만듦
 */
public record Reason(
        ConditionField field,
        String label,
        ReasonStatus status,
        String message,
        List<EvidenceLine> evidence
) {
}
