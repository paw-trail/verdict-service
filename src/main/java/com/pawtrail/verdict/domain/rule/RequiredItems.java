package com.pawtrail.verdict.domain.rule;

import com.pawtrail.verdict.domain.model.Conditions;

import java.util.ArrayList;
import java.util.List;

/**
 * 장소마다 하나인 준비물 목록을 만듭니다. 목록 카드 넷과 검색 카드가 "준비물" 로 보여 줍니다.
 *
 * <pre>
 * 앞   칸에서 따라 나오는 것 — 목줄 필요 → 목줄 · 이동장 필요 → 이동장 · 접종 증명 필요 → 접종 증명서
 * 뒤   조건의 준비물 — 원문에서 읽은 낱말 그대로
 * </pre>
 *
 * 검색 카드의 준비물 칸은 "목줄 필요 표시" 를 하려고 요청된 자리라, 목줄이 칸에만 있어도 준비물에 보여야 합니다.
 * 원문 준비물에 같은 것을 가리키는 말(리드줄 · 켄넬 · 접종 등)이 이미 있으면 더하지 않습니다.
 *
 * 반려견마다 갈리지 않습니다. 반려견이 이미 가진 이동장도 빼지 않는데, 준비물은 장소마다 하나라서입니다.
 */
public final class RequiredItems {

    private static final List<String> LEASH_WORDS = List.of("목줄", "리드줄");
    private static final List<String> CARRIER_WORDS = List.of("이동장", "켄넬", "케이지", "캐리어");
    private static final List<String> VACCINE_WORDS = List.of("접종");

    private RequiredItems() {
    }

    /**
     * @param conditions 장소의 조건 — 조건 행이 없는 장소면 null
     */
    public static List<String> of(Conditions conditions) {
        if (conditions == null) {
            return List.of();
        }
        List<String> given = new ArrayList<>();
        if (conditions.requiredItems() != null) {
            for (String item : conditions.requiredItems()) {
                if (item != null && !item.isBlank() && !given.contains(item.strip())) {
                    given.add(item.strip());
                }
            }
        }

        List<String> items = new ArrayList<>();
        if (Boolean.TRUE.equals(conditions.leashRequired()) && mentionsNone(given, LEASH_WORDS)) {
            items.add("목줄");
        }
        if (Boolean.TRUE.equals(conditions.carrierRequired()) && mentionsNone(given, CARRIER_WORDS)) {
            items.add("이동장");
        }
        if (Boolean.TRUE.equals(conditions.vaccineProof()) && mentionsNone(given, VACCINE_WORDS)) {
            items.add("접종 증명서");
        }
        items.addAll(given);
        return List.copyOf(items);
    }

    private static boolean mentionsNone(List<String> items, List<String> words) {
        return items.stream().noneMatch(item -> words.stream().anyMatch(item::contains));
    }
}
