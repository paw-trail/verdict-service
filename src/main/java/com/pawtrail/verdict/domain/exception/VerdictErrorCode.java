package com.pawtrail.verdict.domain.exception;

import com.pawtrail.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 이 서비스의 에러 코드입니다. 상수 이름이 곧 응답의 code 이며 API 계약입니다.
 *
 * 요청 형식 오류(400 VALIDATION_FAILED)와 인증(401)은 공통 모듈의 코드를 씁니다.
 */
public enum VerdictErrorCode implements ErrorCode {

    // pet 을 부르지 못해 반려동물 정보를 받지 못함
    //
    // * 요청 전체를 실패시키는 이유
    //   반려동물 정보 없이 판정하면 모든 줄이 "확인 필요" 로 떨어지는데
    //   그것은 정상 상태를 뜻하는 값이라 장애가 정상처럼 보임
    //   사용자 서비스는 이 실패를 "불러오지 못함" 으로 안내해 둘을 가름
    //
    // * pet 과 policy 를 코드 하나로 합치지 않는 이유
    //   응답만 보고 어느 쪽이 죽었는지 알 수 있어야 함 (user 의 PET_UNAVAILABLE 과 같은 판단)
    PET_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "반려동물 정보를 불러오지 못했습니다."),

    // policy 를 부르지 못해 동반 조건을 받지 못함 — 위와 같은 까닭으로 요청 전체를 실패시킴
    POLICY_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "동반 조건을 불러오지 못했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    VerdictErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
