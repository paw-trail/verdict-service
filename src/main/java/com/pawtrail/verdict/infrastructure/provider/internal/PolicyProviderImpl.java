package com.pawtrail.verdict.infrastructure.provider.internal;

import com.pawtrail.common.exception.CustomException;
import com.pawtrail.common.response.CommonApiResponse;
import com.pawtrail.verdict.domain.exception.VerdictErrorCode;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import com.pawtrail.verdict.domain.provider.PolicyProvider;
import com.pawtrail.verdict.infrastructure.provider.internal.dto.PolicyBatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 동반 조건을 policy 의 POST /internal/policies/batch 한 번으로 받습니다.
 *
 * 한 번에 500곳이 policy 의 상한이고, 이 서비스가 받는 목록 판정의 상한도 500곳이라 나눠 부를 일이 없습니다.
 * 조건 행이 없는 장소는 policy 가 결과에서 뺍니다. 빈 조건을 지어 담지 않는 것이 저쪽 약속입니다.
 */
@Slf4j
@Component
public class PolicyProviderImpl implements PolicyProvider {

    private static final String BASE_URL = "lb://policy-service";
    private final RestClient restClient;

    public PolicyProviderImpl(@Qualifier("internalRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public Map<UUID, PlaceConditions> findByPlaceIds(List<UUID> placeIds) {
        if (placeIds.isEmpty()) {
            return Map.of();
        }

        CommonApiResponse<List<PolicyBatchResponse>> response;
        try {
            response = restClient.post()
                    .uri("/internal/policies/batch")
                    .body(Map.of("placeIds", placeIds))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.warn("동반 조건을 받아오지 못했습니다: 장소 {}곳, reason={}", placeIds.size(), e.getMessage());
            throw new CustomException(VerdictErrorCode.POLICY_UNAVAILABLE, e);
        }
        if (response == null || response.getData() == null) {
            log.warn("동반 조건 응답이 비어 있습니다: 장소 {}곳", placeIds.size());
            throw new CustomException(VerdictErrorCode.POLICY_UNAVAILABLE);
        }

        Map<UUID, PlaceConditions> places = new LinkedHashMap<>();
        for (PolicyBatchResponse place : response.getData()) {
            if (place != null && place.placeId() != null) {
                places.put(place.placeId(), place.toPlaceConditions());
            }
        }
        return places;
    }
}
