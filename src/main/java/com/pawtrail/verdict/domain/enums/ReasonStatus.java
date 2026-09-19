package com.pawtrail.verdict.domain.enums;

/**
 * 이유 줄 하나의 결과입니다. 화면은 이 값 하나로 줄의 아이콘을 고릅니다.
 *
 * <pre>
 * NOT_MET     그 반려견을 막음                 판정을 불가로
 * MISSING     판정에 필요한데 비어 있음          판정을 확인 필요로
 * CONDITION   가서 지켜야 함                   판정을 조건부로
 * MET         충족
 * INFO        참고 — 판정에 영향 없음 (목줄 · 준비물 · 추가 요금 · 마릿수)
 * </pre>
 *
 * <b>선언 순서가 줄을 늘어놓는 순서입니다.</b> 막힌 이유가 맨 위에 오게 합니다.
 */
public enum ReasonStatus {

    NOT_MET,
    MISSING,
    CONDITION,
    MET,
    INFO;

    /**
     * 이 줄 하나만 두고 보았을 때의 판정입니다. 줄들의 판정 가운데 가장 센 것이 그 반려견의 판정이 됩니다.
     */
    public Verdict toVerdict() {
        return switch (this) {
            case NOT_MET -> Verdict.NOT_ALLOWED;
            case MISSING -> Verdict.UNKNOWN;
            case CONDITION -> Verdict.CONDITIONAL;
            case MET, INFO -> Verdict.ALLOWED;
        };
    }
}
