package com.pawtrail.verdict.domain.enums;

/**
 * 받아 주는 반려견 크기입니다. policy 의 값을 그대로 받습니다.
 *
 * 문장은 policy FieldSpec 의 값 문장과 같은 말입니다.
 */
public enum SizeRule {

    SMALL_ONLY("소형견만"),
    SMALL_MEDIUM("소형 · 중형견"),
    ALL("제한 없음");

    private final String text;

    SizeRule(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    /**
     * 그 크기의 반려견을 받는지 봅니다.
     */
    public boolean accepts(BreedSize size) {
        return switch (this) {
            case SMALL_ONLY -> size == BreedSize.SMALL;
            case SMALL_MEDIUM -> size == BreedSize.SMALL || size == BreedSize.MEDIUM;
            case ALL -> true;
        };
    }
}
