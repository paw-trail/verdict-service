package com.pawtrail.verdict.domain.model;

import java.util.Objects;

/**
 * 근거 한 줄입니다. policy batch 의 근거 줄을 그대로 옮겨 담습니다.
 *
 * 칸마다 그 값을 만든 소스의 근거만 옵니다. 정정이 이긴 장소는 근거가 비어 옵니다.
 *
 * @param fieldName        어느 칸의 근거인지 — 조건 이름(camelCase)
 * @param source           어느 소스의 원문인지 — PET_TOUR · GOCAMPING · CULTURE_CSV
 * @param originField      원문의 어느 키에서 나왔는지
 * @param text             근거 문장 — 원문 그대로
 * @param extractionMethod 규칙이 읽었으면 RULE · 모델이 읽었으면 LLM · 추출 방식이 생기기 전 근거는 null
 */
public record EvidenceLine(
        String fieldName,
        String source,
        String originField,
        String text,
        String extractionMethod
) {

    /**
     * 칸 이름만 빼고 같은 문장인지 봅니다.
     *
     * 체중 제한과 체중 기준처럼 한 문장이 두 칸의 근거가 되면, 두 칸을 한 줄로 보일 때 같은 문장이 두 번 나오지 않게 합니다.
     */
    public boolean sameSentence(EvidenceLine other) {
        return Objects.equals(source, other.source)
                && Objects.equals(originField, other.originField)
                && Objects.equals(text, other.text)
                && Objects.equals(extractionMethod, other.extractionMethod);
    }
}
