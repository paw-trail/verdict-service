package com.pawtrail.verdict.domain.enums;

/**
 * 맹견을 받는 규칙입니다. policy 의 값을 그대로 받습니다.
 *
 * 맹견인 반려견에게만 걸립니다. 문장은 policy FieldSpec 의 값 문장과 같은 말입니다.
 */
public enum BreedRule {

    NONE("제한 없음"),
    DANGEROUS_MUZZLE("맹견은 입마개 착용"),
    DANGEROUS_BANNED("맹견 불가");

    private final String text;

    BreedRule(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
