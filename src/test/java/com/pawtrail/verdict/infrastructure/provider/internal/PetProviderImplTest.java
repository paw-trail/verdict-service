package com.pawtrail.verdict.infrastructure.provider.internal;

import com.pawtrail.common.exception.CustomException;
import com.pawtrail.verdict.domain.enums.BreedSize;
import com.pawtrail.verdict.domain.exception.VerdictErrorCode;
import com.pawtrail.verdict.domain.model.PetProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * pet GET /internal/pets?ids= 를 부르는 모양과 실패 처리를 봅니다.
 */
class PetProviderImplTest {

    private static final UUID DOG = UUID.fromString("0199f000-0000-7000-8000-000000000001");
    private static final UUID GONE = UUID.fromString("0199f000-0000-7000-8000-000000000009");

    private MockRestServiceServer server;
    private PetProviderImpl provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new PetProviderImpl(builder);
    }

    @Test
    @DisplayName("반려동물을 ids 로 한 번에 묻고 판정에 쓰는 칸만 옮김 · pet 이 뺀 반려동물은 결과에 없음")
    void 한_번에_묻는다() {
        server.expect(requestTo("lb://pet-service/internal/pets?ids=" + DOG + "&ids=" + GONE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(envelope("[" + pet("SMALL") + "]"), MediaType.APPLICATION_JSON));

        Map<UUID, PetProfile> pets = provider.findByIds(List.of(DOG, GONE));

        assertThat(pets).containsOnlyKeys(DOG);
        PetProfile pet = pets.get(DOG);
        assertThat(pet.weightKg()).isEqualByComparingTo("5.4");
        assertThat(pet.breedSize()).isEqualTo(BreedSize.SMALL);
        assertThat(pet.hasCarrier()).isTrue();
        assertThat(pet.hasStroller()).isFalse();
        assertThat(pet.vaccineProofAvailable()).isTrue();
        assertThat(pet.dangerousBreed()).isTrue();
        server.verify();
    }

    @Test
    @DisplayName("모르는 크기 값은 비워 둠 — 판정이 그 칸을 정보 없음으로 읽음")
    void 모르는_값은_비워_둠() {
        server.expect(requestTo("lb://pet-service/internal/pets?ids=" + DOG))
                .andRespond(withSuccess(envelope("[" + pet("HUGE") + "]"), MediaType.APPLICATION_JSON));

        assertThat(provider.findByIds(List.of(DOG)).get(DOG).breedSize()).isNull();
    }

    @Test
    @DisplayName("물을 반려동물이 없으면 부르지 않음")
    void 비어_있으면_안_부름() {
        assertThat(provider.findByIds(List.of())).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("pet 을 못 부르면 PET_UNAVAILABLE")
    void 못_부르면_PET_UNAVAILABLE() {
        server.expect(requestTo("lb://pet-service/internal/pets?ids=" + DOG)).andRespond(withServerError());

        assertThatThrownBy(() -> provider.findByIds(List.of(DOG)))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(VerdictErrorCode.PET_UNAVAILABLE));
    }

    private static String pet(String size) {
        return "{\"petId\":\"" + DOG + "\",\"name\":\"콩이\",\"weightKg\":5.4,\"breedSize\":\"" + size + "\","
                + "\"hasCarrier\":true,\"hasStroller\":false,\"vaccineCompleted\":true,\"vaccineProofAvailable\":true,"
                + "\"breedCode\":\"TOSA\",\"breedName\":\"도사견\",\"species\":\"DOG\",\"isDangerousBreed\":true}";
    }

    private static String envelope(String data) {
        return "{\"code\":\"SUCCESS\",\"message\":\"ok\",\"data\":" + data + ",\"traceId\":\"t\"}";
    }
}
