package com.pawtrail.verdict.domain.rule;

import com.pawtrail.verdict.domain.model.Conditions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 장소마다 하나인 준비물 목록을 봅니다. 목록 카드 넷과 검색 카드가 씁니다.
 */
class RequiredItemsTest {

    @Test
    @DisplayName("칸에서 따라 나오는 것을 앞에 두고 원문 준비물을 뒤에 둠")
    void 칸에서_따라_나오는_것이_앞() {
        Conditions conditions = Conditions.builder()
                .leashRequired(true).carrierRequired(true).vaccineProof(true)
                .requiredItems(List.of("배변봉투"))
                .build();

        assertThat(RequiredItems.of(conditions)).containsExactly("목줄", "이동장", "접종 증명서", "배변봉투");
    }

    @Test
    @DisplayName("원문 준비물에 같은 것을 가리키는 말이 있으면 더하지 않음")
    void 같은_말이_있으면_더하지_않음() {
        Conditions conditions = Conditions.builder()
                .leashRequired(true).carrierRequired(true)
                .requiredItems(List.of("리드줄", "켄넬"))
                .build();

        assertThat(RequiredItems.of(conditions)).containsExactly("리드줄", "켄넬");
    }

    @Test
    @DisplayName("빈 값과 겹치는 값은 거르고 앞뒤 공백을 뗌")
    void 빈_값은_거름() {
        Conditions conditions = Conditions.builder()
                .requiredItems(List.of("", "  ", "배변봉투", " 배변봉투 "))
                .build();

        assertThat(RequiredItems.of(conditions)).containsExactly("배변봉투");
    }

    @Test
    @DisplayName("조건 행이 없거나 아무것도 없으면 빈 목록")
    void 없으면_빈_목록() {
        assertThat(RequiredItems.of(null)).isEmpty();
        assertThat(RequiredItems.of(Conditions.builder().leashRequired(false).build())).isEmpty();
    }
}
