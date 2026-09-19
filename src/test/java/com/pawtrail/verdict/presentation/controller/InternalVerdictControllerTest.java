package com.pawtrail.verdict.presentation.controller;

import com.pawtrail.common.enums.Role;
import com.pawtrail.common.exception.CommonErrorCode;
import com.pawtrail.common.exception.CustomException;
import com.pawtrail.common.response.CommonApiResponse;
import com.pawtrail.common.security.principal.CustomUserPrincipal;
import com.pawtrail.verdict.application.dto.output.VerdictBatchOutput;
import com.pawtrail.verdict.application.service.VerdictService;
import com.pawtrail.verdict.domain.provider.PetProvider;
import com.pawtrail.verdict.presentation.request.VerdictBatchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 목록 판정이 사용자 없이 들어온 요청을 pet 을 부르기 전에 막는지 봅니다.
 *
 * 스프링을 띄우지 않고 컨트롤러를 직접 부릅니다. 헤더에서 사용자를 세우는 일은 공통 모듈의 필터가 하므로,
 * 여기서는 필터가 사용자를 못 세운 경우(null)와 세운 경우만 나눠 봅니다.
 */
class InternalVerdictControllerTest {

    private static final UUID ACCOUNT = UUID.fromString("01a0726a-64e9-7712-9ecb-4996e2dcd75f");
    private static final UUID PLACE = UUID.fromString("01a09015-b6bc-7812-8e7e-d0c59c46b007");
    private static final UUID PET = UUID.fromString("0199f000-0000-7000-8000-000000000001");

    @Test
    @DisplayName("사용자 헤더 없이 부르면 pet 을 부르기 전에 401 — pet 장애(502)로 보이지 않게")
    void 사용자가_없으면_401() {
        PetProvider mustNotCall = ids -> {
            throw new AssertionError("pet 을 부르면 안 됨");
        };
        InternalVerdictController controller =
                new InternalVerdictController(new VerdictService(mustNotCall, ids -> Map.of()));

        assertThatThrownBy(() -> controller.judgeBatch(null, new VerdictBatchRequest(List.of(PLACE), List.of(PET))))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonErrorCode.AUTHENTICATION_FAILED));
    }

    @Test
    @DisplayName("사용자가 있으면 판정을 돌려줌")
    void 사용자가_있으면_판정() {
        InternalVerdictController controller =
                new InternalVerdictController(new VerdictService(ids -> Map.of(), ids -> Map.of()));

        ResponseEntity<CommonApiResponse<VerdictBatchOutput>> response = controller.judgeBatch(
                new CustomUserPrincipal(ACCOUNT, Role.USER), new VerdictBatchRequest(List.of(PLACE), List.of(PET)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().results()).hasSize(1);
    }
}
