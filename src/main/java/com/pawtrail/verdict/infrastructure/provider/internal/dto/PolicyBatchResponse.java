package com.pawtrail.verdict.infrastructure.provider.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pawtrail.verdict.domain.enums.BreedRule;
import com.pawtrail.verdict.domain.enums.ExtraFeeUnit;
import com.pawtrail.verdict.domain.enums.Scope;
import com.pawtrail.verdict.domain.enums.SizeRule;
import com.pawtrail.verdict.domain.model.Conditions;
import com.pawtrail.verdict.domain.model.EvidenceLine;
import com.pawtrail.verdict.domain.model.PlaceConditions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * policy POST /internal/policies/batch 응답의 원소입니다.
 *
 * 판 번호(policyVersion)는 받지 않습니다. 캐시 키의 재료인데 이 서비스는 아직 캐시가 없습니다.
 * 근거 줄의 조각 번호도 받지 않습니다. 화면이 근거 문장과 원문 보기로 보여 주므로 쓸 곳이 없습니다.
 *
 * @param correctionSource 관리자 정정이 이긴 장소면 MANUAL · OWNER, 아니면 null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyBatchResponse(
        UUID placeId,
        Fields fields,
        boolean hasConflict,
        String correctionSource,
        List<Evidence> evidence
) {

    public PlaceConditions toPlaceConditions() {
        Conditions conditions = fields == null ? Conditions.builder().build() : fields.toConditions();
        List<EvidenceLine> lines = evidence == null ? List.of()
                : evidence.stream().filter(line -> line != null).map(Evidence::toLine).toList();
        return new PlaceConditions(placeId, conditions, hasConflict, correctionSource, lines);
    }

    /**
     * 조건 20칸입니다. policy 가 null 까지 늘 전부 싣습니다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fields(
            String scope,
            Boolean guideDogOnly,
            Boolean petOnly,
            Boolean indoorAllowed,
            Boolean outdoorAllowed,
            BigDecimal maxWeightKg,
            Boolean weightInclusive,
            Integer maxCount,
            String sizeRule,
            String breedRule,
            Boolean carrierRequired,
            Boolean leashRequired,
            List<String> excludedZones,
            List<String> allowedZonesOnly,
            List<String> excludedDays,
            Integer extraFeeAmount,
            String extraFeeUnit,
            List<String> requiredItems,
            Boolean vaccineProof,
            Boolean advanceInquiry
    ) {

        Conditions toConditions() {
            return Conditions.builder()
                    .scope(EnumValues.of(Scope.class, scope))
                    .guideDogOnly(guideDogOnly)
                    .petOnly(petOnly)
                    .indoorAllowed(indoorAllowed)
                    .outdoorAllowed(outdoorAllowed)
                    .maxWeightKg(maxWeightKg)
                    .weightInclusive(weightInclusive)
                    .maxCount(maxCount)
                    .sizeRule(EnumValues.of(SizeRule.class, sizeRule))
                    .breedRule(EnumValues.of(BreedRule.class, breedRule))
                    .carrierRequired(carrierRequired)
                    .leashRequired(leashRequired)
                    .excludedZones(excludedZones)
                    .allowedZonesOnly(allowedZonesOnly)
                    .excludedDays(excludedDays)
                    .extraFeeAmount(extraFeeAmount)
                    .extraFeeUnit(EnumValues.of(ExtraFeeUnit.class, extraFeeUnit))
                    .requiredItems(requiredItems)
                    .vaccineProof(vaccineProof)
                    .advanceInquiry(advanceInquiry)
                    .build();
        }
    }

    /**
     * 근거 한 줄입니다.
     *
     * @param extractionMethod RULE · LLM — policy v0.1.2 부터 실림 · 그 전에 들어온 근거는 null
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Evidence(
            String fieldName,
            String source,
            String originField,
            String segmentText,
            String extractionMethod
    ) {

        EvidenceLine toLine() {
            return new EvidenceLine(fieldName, source, originField, segmentText, extractionMethod);
        }
    }
}
