package io.gurm.demo.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.gurm.demo.common.BaseResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProfileControllerTest {

    private ProfileService profileService;
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        // 스프링 부트 컨텍스트 로딩 없이 순수 JUnit 5와 Mockito를 활용해 기동 및 주입 수행
        profileService = mock(ProfileService.class);
        profileController = new ProfileController(profileService);
    }

    @Test
    @DisplayName("성공적으로 애플리케이션 및 프로파일 정보를 조회한다")
    void getProfileInfoSuccess() {
        // given
        ProfileResponse mockResponse = new ProfileResponse("spring4-bestpractice", List.of("local"));
        given(profileService.getProfileInfo()).willReturn(mockResponse);

        // when
        ResponseEntity<BaseResponse<ProfileResponse>> response = profileController.getProfileInfo();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        BaseResponse<ProfileResponse> body = response.getBody();
        assertThat(body.getMeta().isSuccess()).isTrue();
        assertThat(body.getMeta().getCode()).isEqualTo("SUCCESS");
        assertThat(body.getMeta().getMessage()).isEqualTo("조회 성공");

        assertThat(body.getData().getApplicationName()).isEqualTo("spring4-bestpractice");
        assertThat(body.getData().getActiveProfiles()).containsExactly("local");
    }

    @Test
    @DisplayName("환경변수 누락 시 예외가 컨트롤러 밖으로 던져진다")
    void getProfileInfoFailure() {
        // given
        given(profileService.getProfileInfo())
                .willThrow(new IllegalStateException("Active profiles config is missing."));

        // when & then
        assertThatThrownBy(() -> profileController.getProfileInfo())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Active profiles config is missing.");
    }

    @Test
    @DisplayName("ExceptionHandler가 IllegalStateException 발생 시 올바른 ProblemDetail 객체를 리턴한다")
    void handleConfigException() {
        // given
        IllegalStateException exception = new IllegalStateException("Active profiles config is missing.");

        // when
        var response = profileController.handleConfigException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        var problemDetail = response.getBody();
        assertThat(problemDetail.getType().toString()).isEqualTo("https://api.gurm.io/errors/config-error");
        assertThat(problemDetail.getTitle()).isEqualTo("Configuration Error");
        assertThat(problemDetail.getStatus()).isEqualTo(500);
        assertThat(problemDetail.getDetail()).isEqualTo("Active profiles config is missing.");
        assertThat(problemDetail.getInstance().toString()).isEqualTo("/profile");
    }
}
