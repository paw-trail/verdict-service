package com.pawtrail.verdict.infrastructure.provider.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pawtrail.verdict.domain.enums.BreedSize;
import com.pawtrail.verdict.domain.model.PetProfile;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * pet GET /internal/pets?ids= 응답의 원소입니다. 판정에 쓰는 칸만 받습니다.
 *
 * 이름 · 견종 · 종 · 접종 완료 여부는 받지 않습니다. 판정에 쓰지 않고, 화면이 GET /pets 로 이미 가집니다.
 *
 * 불리언 넷 가운데 맹견 여부만 Boolean 으로 받습니다.
 * 칸이 빠졌을 때 false 가 되면 이동장 · 유모차 · 접종 증명서는 "없음" 이라 막는 쪽으로 틀리지만,
 * 맹견 여부는 "맹견 아님" 이 되어 허용하는 쪽으로 틀리기 때문입니다. 빠지면 null 로 두고 판정이 "모름" 으로 읽습니다.
 *
 * @param isDangerousBreed 맹견인지. 이름을 JSON 키와 같게 적어 둡니다 — is 로 시작하는 불리언은 도구에 따라 키가 달라질 수 있음
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PetInternalResponse(
        UUID petId,
        BigDecimal weightKg,
        String breedSize,
        boolean hasCarrier,
        boolean hasStroller,
        boolean vaccineProofAvailable,
        @JsonProperty("isDangerousBreed") Boolean isDangerousBreed
) {

    public PetProfile toProfile() {
        return new PetProfile(petId, weightKg, EnumValues.of(BreedSize.class, breedSize),
                hasCarrier, hasStroller, vaccineProofAvailable, isDangerousBreed);
    }
}
