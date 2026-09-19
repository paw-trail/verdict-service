package com.pawtrail.verdict.application.service;

import com.pawtrail.verdict.application.dto.output.PlaceVerdictOutput;
import com.pawtrail.verdict.application.dto.output.PlaceVerdictSummary;
import com.pawtrail.verdict.application.dto.output.VerdictBatchOutput;
import com.pawtrail.verdict.domain.model.PetJudgement;
import com.pawtrail.verdict.domain.model.PetProfile;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import com.pawtrail.verdict.domain.provider.PetProvider;
import com.pawtrail.verdict.domain.provider.PolicyProvider;
import com.pawtrail.verdict.domain.rule.VerdictJudge;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 목록 판정과 장소 상세 판정을 조립합니다.
 *
 * <pre>
 * ① pet 에서 반려동물을 받음          GET /internal/pets?ids= 한 번
 * ② policy 에서 조건을 받음           POST /internal/policies/batch 한 번 · 물을 장소가 없을 때만 건너뜀
 * ③ 장소마다 · 마리마다 판정           VerdictJudge
 * ④ 장소 칸을 붙임                   충돌 여부 · 카드 한 줄 근거 · 준비물 · 정정 출처
 * </pre>
 *
 * <b>pet 과 policy 를 차례로 부릅니다.</b> 병렬로 부르면 다른 스레드에 사용자 정보가 안 넘어가 pet 이 401 을 냅니다.
 * 두 왕복이 정말 병목인지는 부하를 잰 뒤에 봅니다.
 *
 * <b>받은 반려동물이 하나도 없어도 policy 를 부릅니다.</b> 충돌 여부 · 정정 출처 · 준비물은 장소의 사실이라
 * 반려동물과 상관없이 채워야 합니다. 대표 반려동물을 지운 뒤 없는 id 로 불려도 카드의 준비물이 남고,
 * 그때 policy 가 죽어 있으면 "조건 없음" 이 아니라 POLICY_UNAVAILABLE 로 드러납니다.
 *
 * <b>캐시가 없습니다.</b> 부를 때마다 두 서비스에서 받아 새로 판정합니다. 캐시는 부하를 잰 뒤에 붙입니다.
 */
@Service
public class VerdictService {

    private final PetProvider petProvider;
    private final PolicyProvider policyProvider;

    public VerdictService(PetProvider petProvider, PolicyProvider policyProvider) {
        this.petProvider = petProvider;
        this.policyProvider = policyProvider;
    }

    /**
     * 목록 판정 — 요청한 장소마다 하나씩, 요청 순서대로 담습니다. 조건 행이 없는 장소도 담깁니다.
     */
    public VerdictBatchOutput judgeBatch(List<UUID> placeIds, List<UUID> petIds) {
        List<UUID> places = distinct(placeIds);
        List<UUID> pets = distinct(petIds);
        Materials materials = load(places, pets);

        List<PlaceVerdictSummary> results = new ArrayList<>(places.size());
        for (UUID placeId : places) {
            PlaceConditions place = materials.places().get(placeId);
            results.add(PlaceVerdictSummary.of(placeId, place, judge(pets, materials, place)));
        }
        return new VerdictBatchOutput(results);
    }

    /**
     * 장소 상세 판정 — 마리마다 이유 줄까지 담습니다.
     */
    public PlaceVerdictOutput judgePlace(UUID placeId, List<UUID> petIds) {
        List<UUID> pets = distinct(petIds);
        Materials materials = load(List.of(placeId), pets);
        PlaceConditions place = materials.places().get(placeId);
        return PlaceVerdictOutput.of(placeId, place, judge(pets, materials, place));
    }

    private Materials load(List<UUID> placeIds, List<UUID> petIds) {
        Map<UUID, PetProfile> pets = petProvider.findByIds(petIds);
        Map<UUID, PlaceConditions> places = policyProvider.findByPlaceIds(placeIds);
        return new Materials(pets, places);
    }

    private List<PetJudgement> judge(List<UUID> petIds, Materials materials, PlaceConditions place) {
        List<PetJudgement> judgements = new ArrayList<>(petIds.size());
        for (UUID petId : petIds) {
            judgements.add(VerdictJudge.judge(petId, materials.pets().get(petId), place));
        }
        return judgements;
    }

    // 요청 순서를 지키며 중복과 null 을 거름 — policy batch 와 같은 약속
    private static List<UUID> distinct(List<UUID> ids) {
        return new ArrayList<>(new LinkedHashSet<>(ids.stream().filter(Objects::nonNull).toList()));
    }

    private record Materials(Map<UUID, PetProfile> pets, Map<UUID, PlaceConditions> places) {
    }
}
