package com.pawtrail.verdict.application.dto.output;

import java.util.List;

/**
 * 목록 판정의 응답입니다. data 가 배열이 아니라 results 를 감싼 객체입니다.
 *
 * 사용자 서비스와 stub 이 이 모양으로 맞춰져 있어 바꾸지 않습니다.
 *
 * @param results 요청한 장소마다 하나 — 요청 순서 그대로 · 조건 행이 없는 장소도 담김
 */
public record VerdictBatchOutput(List<PlaceVerdictSummary> results) {
}
