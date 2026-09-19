package com.pawtrail.verdict.domain.model;

import com.pawtrail.verdict.domain.enums.BreedRule;
import com.pawtrail.verdict.domain.enums.ExtraFeeUnit;
import com.pawtrail.verdict.domain.enums.Scope;
import com.pawtrail.verdict.domain.enums.SizeRule;

import java.math.BigDecimal;
import java.util.List;

/**
 * 한 장소의 동반 조건 20칸입니다. policy batch 의 fields 를 그대로 옮겨 담습니다.
 *
 * <b>null 은 정보 없음이고 false 와 다릅니다.</b> false 는 "요구하지 않음" 이고 null 은 "원문이 말하지 않음" 입니다.
 * 판정은 둘을 다르게 읽습니다. 크기 · 체중이 둘 다 null 이면 확인 필요로 내는 것이 그 예입니다.
 *
 * 칸이 스물이라 테스트에서 만들기 쉽게 빌더를 둡니다. 도메인을 순수 자바로 두려고 Lombok 을 쓰지 않았습니다.
 */
public record Conditions(
        Scope scope,
        Boolean guideDogOnly,
        Boolean petOnly,
        Boolean indoorAllowed,
        Boolean outdoorAllowed,
        BigDecimal maxWeightKg,
        Boolean weightInclusive,
        Integer maxCount,
        SizeRule sizeRule,
        BreedRule breedRule,
        Boolean carrierRequired,
        Boolean leashRequired,
        List<String> excludedZones,
        List<String> allowedZonesOnly,
        List<String> excludedDays,
        Integer extraFeeAmount,
        ExtraFeeUnit extraFeeUnit,
        List<String> requiredItems,
        Boolean vaccineProof,
        Boolean advanceInquiry
) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Scope scope;
        private Boolean guideDogOnly;
        private Boolean petOnly;
        private Boolean indoorAllowed;
        private Boolean outdoorAllowed;
        private BigDecimal maxWeightKg;
        private Boolean weightInclusive;
        private Integer maxCount;
        private SizeRule sizeRule;
        private BreedRule breedRule;
        private Boolean carrierRequired;
        private Boolean leashRequired;
        private List<String> excludedZones;
        private List<String> allowedZonesOnly;
        private List<String> excludedDays;
        private Integer extraFeeAmount;
        private ExtraFeeUnit extraFeeUnit;
        private List<String> requiredItems;
        private Boolean vaccineProof;
        private Boolean advanceInquiry;

        private Builder() {
        }

        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder guideDogOnly(Boolean guideDogOnly) {
            this.guideDogOnly = guideDogOnly;
            return this;
        }

        public Builder petOnly(Boolean petOnly) {
            this.petOnly = petOnly;
            return this;
        }

        public Builder indoorAllowed(Boolean indoorAllowed) {
            this.indoorAllowed = indoorAllowed;
            return this;
        }

        public Builder outdoorAllowed(Boolean outdoorAllowed) {
            this.outdoorAllowed = outdoorAllowed;
            return this;
        }

        public Builder maxWeightKg(BigDecimal maxWeightKg) {
            this.maxWeightKg = maxWeightKg;
            return this;
        }

        public Builder weightInclusive(Boolean weightInclusive) {
            this.weightInclusive = weightInclusive;
            return this;
        }

        public Builder maxCount(Integer maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public Builder sizeRule(SizeRule sizeRule) {
            this.sizeRule = sizeRule;
            return this;
        }

        public Builder breedRule(BreedRule breedRule) {
            this.breedRule = breedRule;
            return this;
        }

        public Builder carrierRequired(Boolean carrierRequired) {
            this.carrierRequired = carrierRequired;
            return this;
        }

        public Builder leashRequired(Boolean leashRequired) {
            this.leashRequired = leashRequired;
            return this;
        }

        public Builder excludedZones(List<String> excludedZones) {
            this.excludedZones = excludedZones;
            return this;
        }

        public Builder allowedZonesOnly(List<String> allowedZonesOnly) {
            this.allowedZonesOnly = allowedZonesOnly;
            return this;
        }

        public Builder excludedDays(List<String> excludedDays) {
            this.excludedDays = excludedDays;
            return this;
        }

        public Builder extraFeeAmount(Integer extraFeeAmount) {
            this.extraFeeAmount = extraFeeAmount;
            return this;
        }

        public Builder extraFeeUnit(ExtraFeeUnit extraFeeUnit) {
            this.extraFeeUnit = extraFeeUnit;
            return this;
        }

        public Builder requiredItems(List<String> requiredItems) {
            this.requiredItems = requiredItems;
            return this;
        }

        public Builder vaccineProof(Boolean vaccineProof) {
            this.vaccineProof = vaccineProof;
            return this;
        }

        public Builder advanceInquiry(Boolean advanceInquiry) {
            this.advanceInquiry = advanceInquiry;
            return this;
        }

        public Conditions build() {
            return new Conditions(scope, guideDogOnly, petOnly, indoorAllowed, outdoorAllowed, maxWeightKg, weightInclusive, maxCount, sizeRule, breedRule, carrierRequired, leashRequired, excludedZones, allowedZonesOnly, excludedDays, extraFeeAmount, extraFeeUnit, requiredItems, vaccineProof, advanceInquiry);
        }
    }
}
