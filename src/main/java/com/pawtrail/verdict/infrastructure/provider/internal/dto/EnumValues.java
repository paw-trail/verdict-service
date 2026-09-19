package com.pawtrail.verdict.infrastructure.provider.internal.dto;

/**
 * 다른 서비스가 보낸 글자를 이 서비스의 열거값으로 바꿉니다.
 *
 * 모르는 값이면 예외 대신 null 을 돌려줍니다.
 * 저쪽이 값을 하나 늘렸다고 판정 전체가 실패하면 목록 배지가 통째로 "불러오지 못함" 이 되는데,
 * null 이면 그 칸만 "정보 없음" 으로 읽혀 판정이 보수적으로 떨어집니다.
 */
public final class EnumValues {

    private EnumValues() {
    }

    public static <E extends Enum<E>> E of(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.strip());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
