package com.pawtrail.verdict.application.dto.output;

import com.pawtrail.verdict.domain.model.EvidenceLine;

/**
 * 이유 줄의 근거 한 줄입니다.
 *
 * 화면이 출처 말을 만듭니다 — RULE 이면 "공공데이터 항목", LLM 이면 "안내문을 AI 가 읽음".
 * 소스 이름(한국관광공사 등)은 장소 상세 응답의 sources[] 로 붙입니다.
 *
 * @param source           PET_TOUR · GOCAMPING · CULTURE_CSV
 * @param originField      원문의 어느 키에서 나왔는지
 * @param text             근거 문장 — 원문 그대로
 * @param extractionMethod RULE · LLM · 추출 방식이 생기기 전 근거는 null
 */
public record EvidenceOutput(
        String source,
        String originField,
        String text,
        String extractionMethod
) {

    public static EvidenceOutput from(EvidenceLine line) {
        return new EvidenceOutput(line.source(), line.originField(), line.text(), line.extractionMethod());
    }
}
