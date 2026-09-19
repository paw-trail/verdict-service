package com.pawtrail.verdict.domain.enums;

/**
 * 추가 요금을 무엇마다 받는지입니다. policy 의 값을 그대로 받습니다.
 */
public enum ExtraFeeUnit {

    PER_DOG("마리당"),
    PER_NIGHT("1박당"),
    PER_VISIT("방문당");

    private final String text;

    ExtraFeeUnit(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
