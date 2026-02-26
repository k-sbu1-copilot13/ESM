package com.example.esm_project.service;

import com.example.esm_project.dto.LoginRequest;
import com.example.esm_project.dto.LoginResponse;
import com.example.esm_project.dto.RefreshTokenRequest;
import com.example.esm_project.dto.RefreshTokenResponse;
import com.example.esm_project.entity.RefreshToken;
import com.example.esm_project.entity.User;
import com.example.esm_project.exception.AccountLockedException;
import com.example.esm_project.exception.TokenRefreshException;
import com.example.esm_project.repository.UserRepository;
import com.example.esm_project.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito, KHÔNG load Spring context
class AuthServiceTest {

    // ---- MOCK các dependency (object giả, không gọi DB/logic thật) ----
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    // ---- Class cần test — Mockito sẽ tự inject các @Mock ở trên vào đây ----
    @InjectMocks
    private AuthService authService;

    // ---- Dữ liệu dùng chung cho nhiều test ----
    private User activeUser;
    private User lockedUser;
    private LoginRequest validRequest;

    @BeforeEach
    void setUp() {
        // Dữ liệu mẫu — chạy trước MỖI test case
        activeUser = new User(1L, "john", "hashed_password", "John Doe", "EMPLOYEE", "ACTIVE");
        lockedUser = new User(2L, "jane", "hashed_password", "Jane Doe", "EMPLOYEE", "LOCKED");
        validRequest = new LoginRequest("john", "raw_password");
    }

    // =========================================================
    // TEST login()
    // =========================================================

    @Test
    @DisplayName("Login thành công: trả về access token và refresh token")
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        // ARRANGE
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token-uuid")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("raw_password", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "john", "EMPLOYEE")).thenReturn("access-jwt-token");
        when(refreshTokenService.createRefreshToken(activeUser)).thenReturn(refreshToken);

        // ACT
        LoginResponse response = authService.login(validRequest);

        // ASSERT — dùng đúng tên field của LoginResponse (token, không phải
        // accessToken)
        assertThat(response.getToken()).isEqualTo("access-jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getRole()).isEqualTo("EMPLOYEE");

        // VERIFY — đảm bảo các dependency được gọi đúng
        verify(userRepository).findByUsername("john");
        verify(passwordEncoder).matches("raw_password", "hashed_password");
        verify(jwtUtil).generateToken(1L, "john", "EMPLOYEE");
        verify(refreshTokenService).createRefreshToken(activeUser);
    }

    @Test
    @DisplayName("Login thất bại: không tìm thấy username")
    void login_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // ARRANGE
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(UsernameNotFoundException.class,
                () -> authService.login(new LoginRequest("ghost", "any")));

        // Đảm bảo không gọi bất cứ dependency nào khác khi user không tồn tại
        verifyNoInteractions(passwordEncoder, jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("Login thất bại: tài khoản bị khóa (LOCKED)")
    void login_shouldThrowAccountLockedException_whenAccountIsLocked() {
        // ARRANGE
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(lockedUser));

        // ACT + ASSERT
        assertThrows(AccountLockedException.class,
                () -> authService.login(new LoginRequest("jane", "any")));

        // Tài khoản bị lock → không check password, không tạo token
        verifyNoInteractions(passwordEncoder, jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("Login thất bại: sai mật khẩu")
    void login_shouldThrowBadCredentialsException_whenPasswordIsWrong() {
        // ARRANGE
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        // ACT + ASSERT
        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("john", "wrong_password")));

        // Sai password → không tạo token
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }

    // =========================================================
    // TEST refreshAccessToken()
    // =========================================================

    @Test
    @DisplayName("Refresh token thành công: trả về access token và refresh token mới")
    void refreshAccessToken_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        // ARRANGE
        RefreshToken oldToken = RefreshToken.builder()
                .token("old-refresh-token")
                .user(activeUser)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .token("new-refresh-token")
                .build();

        when(refreshTokenService.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));
        when(refreshTokenService.verifyExpiration(oldToken)).thenReturn(oldToken);
        when(jwtUtil.generateToken(1L, "john", "EMPLOYEE")).thenReturn("new-access-jwt");
        when(refreshTokenService.createRefreshToken(activeUser)).thenReturn(newToken);

        // ACT
        RefreshTokenResponse response = authService.refreshAccessToken(
                new RefreshTokenRequest("old-refresh-token"));

        // ASSERT
        assertThat(response.getAccessToken()).isEqualTo("new-access-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

        // VERIFY Token Rotation — token cũ phải bị revoke
        verify(refreshTokenService).revokeToken(oldToken);
        verify(refreshTokenService).createRefreshToken(activeUser);
    }

    @Test
    @DisplayName("Refresh token thất bại: token không tồn tại trong DB")
    void refreshAccessToken_shouldThrowTokenRefreshException_whenTokenNotFound() {
        // ARRANGE
        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(TokenRefreshException.class,
                () -> authService.refreshAccessToken(new RefreshTokenRequest("invalid-token")));

        verify(refreshTokenService, never()).revokeToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}
