package com.pawtrail.verdict.presentation.controller;

import com.pawtrail.common.response.CommonApiResponse;
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
 * 부르는 쪽이 사용자의 X-User-Id 를 실어 보내야 합니다. 그 값이 pet 호출에 그대로 따라가 남의 반려동물을 걸러 냅니다.
 */
@RestController
@RequestMapping("/internal/verdicts")
@RequiredArgsConstructor
public class InternalVerdictController {

    private final VerdictService verdictService;

    @PostMapping("/batch")
    public ResponseEntity<CommonApiResponse<VerdictBatchOutput>> judgeBatch(
            @Valid @RequestBody VerdictBatchRequest request) {
        return ResponseEntity.ok(CommonApiResponse.success(
                verdictService.judgeBatch(request.placeIds(), request.petIds())));
    }
}
