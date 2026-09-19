package com.pawtrail.verdict.domain.model;

import com.pawtrail.verdict.domain.enums.BreedSize;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 판정에 쓰는 반려동물 정보입니다. pet 의 internal 조회에서 판정에 필요한 칸만 옮겨 담습니다.
 *
 * 종(species)은 담지 않습니다. 조건 데이터가 반려견 기준이라 모든 반려동물을 개 기준으로 판정하고,
 * 개가 아니면 화면이 장소 상세 위에 안내 문구를 띄웁니다.
 *
 * @param petId                 반려동물 식별자
 * @param weightKg              체중. 비어 있을 수 있음
 * @param breedSize             크기. 비어 있을 수 있음
 * @param hasCarrier            이동장이 있는지
 * @param hasStroller           유모차가 있는지 — 이동장 필요 조건을 유모차로도 채움
 * @param vaccineProofAvailable 접종 증명서가 있는지
 * @param dangerousBreed        맹견인지
 */
public record PetProfile(
        UUID petId,
        BigDecimal weightKg,
        BreedSize breedSize,
        boolean hasCarrier,
        boolean hasStroller,
        boolean vaccineProofAvailable,
        boolean dangerousBreed
) {
}
