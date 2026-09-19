package com.pawtrail.verdict.infrastructure.provider.internal;

import com.pawtrail.common.exception.CustomException;
import com.pawtrail.common.response.CommonApiResponse;
import com.pawtrail.verdict.domain.exception.VerdictErrorCode;
import com.pawtrail.verdict.domain.model.PetProfile;
import com.pawtrail.verdict.domain.provider.PetProvider;
import com.pawtrail.verdict.infrastructure.provider.internal.dto.PetInternalResponse;
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
 * 반려동물 정보를 pet 의 GET /internal/pets?ids= 한 번으로 받습니다.
 *
 * 마리 수와 상관없이 왕복 하나입니다. 단건 조회를 마리마다 부르면 왕복이 마리 수만큼 늘어납니다.
 *
 * <b>인증 헤더가 따라가야 합니다.</b> pet 의 /internal 은 X-User-Id 가 없으면 401 이고,
 * 남의 반려동물은 조용히 빼 줍니다. 공통 모듈의 인터셉터가 요청을 처리하는 스레드의 사용자를 꺼내 실어 보내므로,
 * 이 호출을 다른 스레드로 넘기면(병렬 호출) 헤더가 빠져 401 이 납니다. 그래서 pet 과 policy 를 차례로 부릅니다.
 */
@Slf4j
@Component
public class PetProviderImpl implements PetProvider {

    private static final String BASE_URL = "lb://pet-service";
    private final RestClient restClient;

    /**
     * @Qualifier 를 반드시 붙입니다. 같은 타입의 빌더가 여럿이고 아무것도 얹히지 않은 쪽이 @Primary 라,
     * 빠뜨리면 lb:// 를 풀지 못하는 빌더가 조용히 주입되어 기동이 아니라 부르는 순간에 실패합니다.
     */
    public PetProviderImpl(@Qualifier("internalRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public Map<UUID, PetProfile> findByIds(List<UUID> petIds) {
        if (petIds.isEmpty()) {
            return Map.of();
        }

        CommonApiResponse<List<PetInternalResponse>> response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/internal/pets").queryParam("ids", petIds).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.warn("반려동물 정보를 받아오지 못했습니다: {}마리, reason={}", petIds.size(), e.getMessage());
            throw new CustomException(VerdictErrorCode.PET_UNAVAILABLE, e);
        }
        if (response == null || response.getData() == null) {
            log.warn("반려동물 응답이 비어 있습니다: {}마리", petIds.size());
            throw new CustomException(VerdictErrorCode.PET_UNAVAILABLE);
        }

        Map<UUID, PetProfile> pets = new LinkedHashMap<>();
        for (PetInternalResponse pet : response.getData()) {
            if (pet != null && pet.petId() != null) {
                pets.put(pet.petId(), pet.toProfile());
            }
        }
        return pets;
    }
}
