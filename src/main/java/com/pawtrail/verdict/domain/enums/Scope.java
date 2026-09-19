package com.pawtrail.verdict.domain.enums;

/**
 * 동반 범위입니다. policy 의 값을 그대로 받습니다.
 *
 * UNKNOWN 은 extract 가 쓰지 않으나 관리자 정정으로 들어올 수 있어 받아 둡니다. 판정은 비어 있는 것과 같게 봅니다.
 */
public enum Scope {

    ALL_AREA,
    PARTIAL,
    NONE,
    UNKNOWN
}
