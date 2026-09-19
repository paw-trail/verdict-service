package com.pawtrail.verdict.presentation.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 목록 판정 요청의 상한을 봅니다. 스프링을 띄우지 않고 컨트롤러의 @Valid 가 부르는 것과 같은 검증기만 씁니다.
 */
class VerdictBatchRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("장소 500곳 · 반려동물 100마리까지 통과하고 빈 목록도 통과")
    void 상한_안이면_통과() {
        assertThat(validator.validate(new VerdictBatchRequest(ids(500), ids(100)))).isEmpty();
        assertThat(validator.validate(new VerdictBatchRequest(List.of(), List.of()))).isEmpty();
    }

    @Test
    @DisplayName("장소가 500곳을 넘으면 그 칸을 가리켜 막음")
    void 장소가_많으면_막힘() {
        Set<ConstraintViolation<VerdictBatchRequest>> violations =
                validator.validate(new VerdictBatchRequest(ids(501), ids(1)));

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("placeIds");
    }

    @Test
    @DisplayName("반려동물이 100마리를 넘으면 그 칸을 가리켜 막음")
    void 반려동물이_많으면_막힘() {
        Set<ConstraintViolation<VerdictBatchRequest>> violations =
                validator.validate(new VerdictBatchRequest(ids(1), ids(101)));

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("petIds");
    }

    @Test
    @DisplayName("목록이 아예 없으면 막음")
    void 목록이_없으면_막힘() {
        assertThat(validator.validate(new VerdictBatchRequest(null, null))).hasSize(2);
    }

    private static List<UUID> ids(int count) {
        return Stream.generate(UUID::randomUUID).limit(count).toList();
    }
}
