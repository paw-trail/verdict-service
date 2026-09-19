package com.pawtrail.verdict.presentation.controller;

import com.pawtrail.common.exception.CommonErrorCode;
import com.pawtrail.common.exception.CustomException;
import com.pawtrail.common.response.CommonApiResponse;
import com.pawtrail.common.security.annotation.CurrentUser;
import com.pawtrail.common.security.principal.CustomUserPrincipal;
import com.pawtrail.verdict.application.dto.output.VerdictBatchOutput;
import com.pawtrail.verdict.application.service.VerdictService;
import com.pawtrail.verdict.presentation.request.VerdictBatchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 다른 서비스가 부르는 목록 판정입니다. 게이트웨이를 거치지 않습니다.
 *
 * <b>부르는 쪽이 사용자의 X-User-Id · X-User-Role 을 실어 보내야 합니다.</b>
 * 그 값이 pet 호출에 그대로 따라가 남의 반려동물을 걸러 냅니다.
 *
 * <b>사용자가 없으면 pet 을 부르기 전에 401 로 막습니다.</b>
 * /internal 은 보안 설정이 열어 두어 헤더 없이도 여기까지 들어옵니다.
 * 막지 않으면 pet 을 헤더 없이 부르고, pet 이 낸 401 을 받아 "pet 을 못 부름(502)" 으로 전하게 되어
 * 부르는 쪽의 실수가 pet 장애처럼 보입니다. pet 의 internal 조회가 같은 자리를 같은 코드로 막습니다.
 */
@RestController
@RequestMapping("/internal/verdicts")
@RequiredArgsConstructor
public class InternalVerdictController {

    private final VerdictService verdictService;

    @PostMapping("/batch")
    public ResponseEntity<CommonApiResponse<VerdictBatchOutput>> judgeBatch(
            @CurrentUser CustomUserPrincipal principal,
            @Valid @RequestBody VerdictBatchRequest request) {
        requireUser(principal);
        return ResponseEntity.ok(CommonApiResponse.success(
                verdictService.judgeBatch(request.placeIds(), request.petIds())));
    }

    private static void requireUser(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new CustomException(CommonErrorCode.AUTHENTICATION_FAILED);
        }
    }
}
