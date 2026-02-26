package com.example.esm_project.controller;

import com.example.esm_project.entity.User;
import com.example.esm_project.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test — AuthController
 *
 * Luồng test: MockMvc → Security Filter → AuthController → AuthService → H2 DB
 *
 * Test các scenarios:
 * - POST /api/auth/login (login, sai password, user không tồn tại, blank
 * validation, locked)
 * - POST /api/auth/refresh (refresh token hợp lệ, sai/hết hạn)
 */
@SpringBootTest // Load toàn bộ Spring context
@AutoConfigureMockMvc // Tự tạo MockMvc bean
@ActiveProfiles("test") // Kích hoạt application-test.yml → dùng H2
@Transactional // Rollback DB sau mỗi test → mỗi test độc lập
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        // -------------------------------------------------------------------------
        // Setup: tạo user test trong H2 trước mỗi test
        // @Transactional rollback sau → setUp chạy lại cho test tiếp theo
        // -------------------------------------------------------------------------
        @BeforeEach
        void setUp() {
                User activeUser = new User();
                activeUser.setUsername("employee_test");
                activeUser.setPassword(passwordEncoder.encode("password123"));
                activeUser.setFullName("Test Employee");
                activeUser.setRole("EMPLOYEE");
                activeUser.setStatus("ACTIVE");
                userRepository.save(activeUser);

                User lockedUser = new User();
                lockedUser.setUsername("locked_user");
                lockedUser.setPassword(passwordEncoder.encode("password123"));
                lockedUser.setFullName("Locked User");
                lockedUser.setRole("EMPLOYEE");
                lockedUser.setStatus("LOCKED");
                userRepository.save(lockedUser);
        }

        // =========================================================================
        // POST /api/auth/login
        // =========================================================================
        @Nested
        @DisplayName("POST /api/auth/login")
        class LoginTests {

                @Test
                @DisplayName("200 OK — credentials hợp lệ, trả về token")
                void login_success() throws Exception {
                        Map<String, String> body = Map.of(
                                        "username", "employee_test",
                                        "password", "password123");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.token").isNotEmpty())
                                        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                                        .andExpect(jsonPath("$.username").value("employee_test"))
                                        .andExpect(jsonPath("$.role").value("EMPLOYEE"));
                }

                @Test
                @DisplayName("401 Unauthorized — sai mật khẩu")
                void login_wrongPassword() throws Exception {
                        Map<String, String> body = Map.of(
                                        "username", "employee_test",
                                        "password", "wrong-password");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("401 Unauthorized — username không tồn tại")
                void login_userNotFound() throws Exception {
                        Map<String, String> body = Map.of(
                                        "username", "nonexistent_user",
                                        "password", "password123");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("403 Forbidden — account bị LOCKED")
                void login_accountLocked() throws Exception {
                        Map<String, String> body = Map.of(
                                        "username", "locked_user",
                                        "password", "password123");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("400 Bad Request — username blank (@NotBlank validation)")
                void login_usernameBlank() throws Exception {
                        Map<String, String> body = Map.of(
                                        "username", "",
                                        "password", "password123");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("400 Bad Request — password blank (@NotBlank validation)")
                void login_passwordBlank() throws Exception {
                        Map<String, String> body = Map.of(
                                        "username", "employee_test",
                                        "password", "");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(body)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("400 Bad Request — body rỗng {}")
                void login_emptyBody() throws Exception {
                        // Gửi JSON rỗng "{}" → thiếu cả username lẫn password → 400 @Valid
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}"))
                                        .andExpect(status().isBadRequest());
                }
        }
}
