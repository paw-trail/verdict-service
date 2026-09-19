package com.pawtrail.verdict.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * 목록 판정 요청입니다. 사용자 서비스가 장소를 500곳씩 잘라 보냅니다.
 *
 * 장소 500곳은 policy batch 의 상한과 같아 이 서비스가 나눠 부를 일이 없고,
 * 반려동물 100마리는 pet 의 ?ids= 상한과 같습니다.
 * 목록이 비어 있어도 막지 않습니다. 부르지 않고 빈 결과를 돌려줍니다.
 *
 * @param placeIds 판정할 장소들 — 중복과 null 은 서비스가 걸러 냄
 * @param petIds   판정할 반려동물들 — 함께 가는 무리가 아니라 판정 기준 목록임
 */
public record VerdictBatchRequest(

        @NotNull(message = "장소 식별자 목록은 필수입니다.")
        @Size(max = 500, message = "한 번에 판정할 수 있는 장소는 500곳입니다.")
        List<UUID> placeIds,

        @NotNull(message = "반려동물 식별자 목록은 필수입니다.")
        @Size(max = 100, message = "한 번에 판정할 수 있는 반려동물은 100마리입니다.")
        List<UUID> petIds
) {
}
