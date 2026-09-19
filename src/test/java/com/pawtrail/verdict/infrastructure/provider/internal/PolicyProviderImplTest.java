package com.pawtrail.verdict.infrastructure.provider.internal;

import com.pawtrail.common.exception.CustomException;
import com.pawtrail.verdict.domain.enums.Scope;
import com.pawtrail.verdict.domain.enums.SizeRule;
import com.pawtrail.verdict.domain.exception.VerdictErrorCode;
import com.pawtrail.verdict.domain.model.EvidenceLine;
import com.pawtrail.verdict.domain.model.PlaceConditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * policy POST /internal/policies/batch 를 부르는 모양과 실패 처리를 봅니다.
 */
class PolicyProviderImplTest {

    private static final String BATCH = "lb://policy-service/internal/policies/batch";
    private static final UUID PLACE = UUID.fromString("01a09015-b6bc-7812-8e7e-d0c59c46b007");
    private static final UUID NO_ROW = UUID.fromString("01a09015-0000-7000-8000-000000000002");

    private MockRestServiceServer server;
    private PolicyProviderImpl provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new PolicyProviderImpl(builder);
    }

    @Test
    @DisplayName("장소들을 한 번에 묻고 조건 · 충돌 여부 · 근거와 추출 방식을 옮김 · 조건 행이 없는 장소는 결과에 없음")
    void 한_번에_묻는다() {
        server.expect(requestTo(BATCH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.placeIds[0]").value(PLACE.toString()))
                .andExpect(jsonPath("$.placeIds[1]").value(NO_ROW.toString()))
                .andRespond(withSuccess(envelope("[" + place("PARTIAL") + "]"), MediaType.APPLICATION_JSON));

        Map<UUID, PlaceConditions> places = provider.findByPlaceIds(List.of(PLACE, NO_ROW));

        assertThat(places).containsOnlyKeys(PLACE);
        PlaceConditions place = places.get(PLACE);
        assertThat(place.hasConflict()).isTrue();
        assertThat(place.correctionSource()).isNull();
        assertThat(place.conditions().scope()).isEqualTo(Scope.PARTIAL);
        assertThat(place.conditions().sizeRule()).isEqualTo(SizeRule.SMALL_ONLY);
        assertThat(place.conditions().leashRequired()).isTrue();
        assertThat(place.conditions().requiredItems()).containsExactly("배변봉투");
        assertThat(place.evidence()).extracting(EvidenceLine::text).containsExactly("일부구역 동반가능", "소형견만 출입 허용");
        assertThat(place.evidence()).extracting(EvidenceLine::extractionMethod).containsExactly("RULE", "LLM");
        server.verify();
    }

    @Test
    @DisplayName("모르는 범위 값은 비워 둠 — 판정이 동반 자체를 모르는 것으로 읽음")
    void 모르는_값은_비워_둠() {
        server.expect(requestTo(BATCH))
                .andRespond(withSuccess(envelope("[" + place("SOMETHING_NEW") + "]"), MediaType.APPLICATION_JSON));

        assertThat(provider.findByPlaceIds(List.of(PLACE)).get(PLACE).conditions().scope()).isNull();
    }

    @Test
    @DisplayName("물을 장소가 없으면 부르지 않음")
    void 비어_있으면_안_부름() {
        assertThat(provider.findByPlaceIds(List.of())).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("policy 가 받지 않으면 POLICY_UNAVAILABLE")
    void 못_부르면_POLICY_UNAVAILABLE() {
        server.expect(requestTo(BATCH)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> provider.findByPlaceIds(List.of(PLACE)))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(VerdictErrorCode.POLICY_UNAVAILABLE));
    }

    private static String place(String scope) {
        return "{\"placeId\":\"" + PLACE + "\","
                + "\"fields\":{\"scope\":\"" + scope + "\",\"guideDogOnly\":null,\"petOnly\":false,"
                + "\"indoorAllowed\":null,\"outdoorAllowed\":true,\"maxWeightKg\":null,\"weightInclusive\":null,"
                + "\"maxCount\":null,\"sizeRule\":\"SMALL_ONLY\",\"breedRule\":null,\"carrierRequired\":null,"
                + "\"leashRequired\":true,\"excludedZones\":null,\"allowedZonesOnly\":null,\"excludedDays\":null,"
                + "\"extraFeeAmount\":0,\"extraFeeUnit\":null,\"requiredItems\":[\"배변봉투\"],"
                + "\"vaccineProof\":null,\"advanceInquiry\":null},"
                + "\"hasConflict\":true,\"policyVersion\":3,\"correctionSource\":null,"
                + "\"evidence\":["
                + "{\"fieldName\":\"scope\",\"source\":\"PET_TOUR\",\"originField\":\"acmpyTypeCd\","
                + "\"segmentIndex\":null,\"segmentText\":\"일부구역 동반가능\",\"extractionMethod\":\"RULE\"},"
                + "{\"fieldName\":\"sizeRule\",\"source\":\"GOCAMPING\",\"originField\":\"intro\","
                + "\"segmentIndex\":3,\"segmentText\":\"소형견만 출입 허용\",\"extractionMethod\":\"LLM\"}]}";
    }

    private static String envelope(String data) {
        return "{\"code\":\"SUCCESS\",\"message\":\"ok\",\"data\":" + data + ",\"traceId\":\"t\"}";
    }
}
