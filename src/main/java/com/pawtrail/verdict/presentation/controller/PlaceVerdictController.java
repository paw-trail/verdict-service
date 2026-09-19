package com.pawtrail.verdict.presentation.controller;

import com.pawtrail.common.response.CommonApiResponse;
import com.pawtrail.verdict.application.dto.output.PlaceVerdictOutput;
import com.pawtrail.verdict.application.service.VerdictService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 장소 상세의 판정입니다. 게이트웨이를 거쳐 브라우저가 부릅니다.
 *
 * <b>petIds 는 필수입니다.</b> 빠지면 대표 반려동물을 쓰는 대신 400 을 냅니다.
 * 화면이 대표 반려동물을 이미 알고 있어(GET /users/me) 이 서비스가 사용자 서비스에 다시 물을 까닭이 없고,
 * 빠뜨린 실수가 조용히 넘어가지 않고 바로 드러납니다.
 */
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Validated
public class PlaceVerdictController {

    private final VerdictService verdictService;

    @GetMapping("/{placeId}/verdict")
    public ResponseEntity<CommonApiResponse<PlaceVerdictOutput>> getVerdict(
            @PathVariable UUID placeId,
            @RequestParam("petIds")
            @NotEmpty(message = "판정할 반려동물을 하나 이상 보내야 합니다.")
            @Size(max = 100, message = "한 번에 판정할 수 있는 반려동물은 100마리입니다.")
            List<UUID> petIds) {
        return ResponseEntity.ok(CommonApiResponse.success(verdictService.judgePlace(placeId, petIds)));
    }
}
