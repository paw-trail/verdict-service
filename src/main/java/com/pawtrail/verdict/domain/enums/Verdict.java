package com.pawtrail.verdict.domain.enums;

/**
 * 판정 네 단계입니다.
 *
 * 이름이 곧 응답 값이며 사용자 서비스가 같은 넷만 받습니다.
 * 모르는 값이 오면 방문 기록이 502 로 막히므로 값을 더하거나 이름을 바꾸지 않습니다.
 *
 * <b>선언 순서가 제한이 센 순서입니다.</b> 불가 → 확인 필요 → 조건부 → 가능.
 * 이유 줄의 결과를 모을 때와, 여러 마리 가운데 카드 한 줄 근거의 기준이 될 마리를 고를 때 이 순서를 씁니다.
 */
public enum Verdict {

    // 그 반려견은 들어갈 수 없음
    NOT_ALLOWED,

    // 판정에 필요한 정보가 비어 있음 — 화면 문구는 "확인 필요"
    UNKNOWN,

    // 들어갈 수 있으나 가서 지킬 것이 있음 — 이유 줄을 읽어야 함
    CONDITIONAL,

    // 들어갈 수 있음
    ALLOWED;

    /**
     * 둘 가운데 제한이 더 센 쪽입니다.
     */
    public Verdict stricter(Verdict other) {
        return compareTo(other) <= 0 ? this : other;
    }
}
