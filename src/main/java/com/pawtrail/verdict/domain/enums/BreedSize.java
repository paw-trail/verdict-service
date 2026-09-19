package com.pawtrail.verdict.domain.enums;

/**
 * 반려견 크기입니다. pet 의 값을 그대로 받습니다.
 *
 * pet 이 체중으로 채우고(10kg 미만 소형 · 25kg 미만 중형 · 그 이상 대형) 사용자가 고칠 수 있습니다.
 * 이 서비스는 체중에서 다시 계산하지 않고 받은 값을 씁니다.
 */
public enum BreedSize {

    SMALL("소형견"),
    MEDIUM("중형견"),
    LARGE("대형견");

    private final String text;

    BreedSize(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
