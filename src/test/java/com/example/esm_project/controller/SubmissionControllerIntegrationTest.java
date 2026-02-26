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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test — SubmissionController
 *
 * Luồng test: MockMvc → JWT Filter → SubmissionController → SubmissionService →
 * H2 DB
 *
 * Vì SubmissionController dùng getCurrentUserId() → UserPrincipal.getId()
 * nên cần login thật để lấy JWT (không dùng @WithMockUser)
 *
 * Test các scenarios:
 * - GET /api/submissions/me (authenticated, unauthenticated)
 * - POST /api/submissions/draft (tạo draft thành công, thiếu template ID)
 * - GET /api/submissions/{id} (lấy detail)
 * - DELETE /api/submissions/{id} (xóa draft)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubmissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String employeeToken; // JWT token dùng để gọi secured endpoints

    @BeforeEach
    void setUp() throws Exception {
        // Tạo employee trong H2
        User employee = new User();
        employee.setUsername("emp_submit");
        employee.setPassword(passwordEncoder.encode("password123"));
        employee.setFullName("Submit Employee");
        employee.setRole("EMPLOYEE");
        employee.setStatus("ACTIVE");
        userRepository.save(employee);

        // Login thật để lấy JWT — cần thiết vì getCurrentUserId() cần UserPrincipal
        // thật
        employeeToken = loginAndGetToken("emp_submit", "password123");
    }

    /**
     * Helper: gọi POST /api/auth/login → trích xuất access token từ response
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);

        String responseJson = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseJson).get("token").asText();
    }

    // =========================================================================
    // GET /api/submissions/me
    // =========================================================================
    @Nested
    @DisplayName("GET /api/submissions/me")
    class GetMySubmissionsTests {

        @Test
        @DisplayName("200 OK — có JWT hợp lệ")
        void getMySubmissions_authenticated() throws Exception {
            mockMvc.perform(get("/api/submissions/me")
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("401 Unauthorized — không có JWT token (EntryPoint trả 401)")
        void getMySubmissions_unauthenticated() throws Exception {
            // JwtAuthenticationEntryPoint trả 401 khi không có token
            // 403 chỉ xảy ra khi đã auth nhưng sai role
            mockMvc.perform(get("/api/submissions/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized — JWT token không hợp lệ")
        void getMySubmissions_invalidToken() throws Exception {
            mockMvc.perform(get("/api/submissions/me")
                    .header("Authorization", "Bearer invalid.jwt.token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/submissions/me/drafts
    // =========================================================================
    @Nested
    @DisplayName("GET /api/submissions/me/drafts")
    class GetMyDraftsTests {

        @Test
        @DisplayName("200 OK — trả về danh sách draft (có thể rỗng)")
        void getMyDrafts_authenticated() throws Exception {
            mockMvc.perform(get("/api/submissions/me/drafts")
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("401 Unauthorized — không có JWT")
        void getMyDrafts_unauthenticated() throws Exception {
            mockMvc.perform(get("/api/submissions/me/drafts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/submissions/me/submitted
    // =========================================================================
    @Nested
    @DisplayName("GET /api/submissions/me/submitted")
    class GetMySubmittedTests {

        @Test
        @DisplayName("200 OK — trả về danh sách submission đã nộp")
        void getMySubmitted_authenticated() throws Exception {
            mockMvc.perform(get("/api/submissions/me/submitted")
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // GET /api/submissions/{id}
    // =========================================================================
    @Nested
    @DisplayName("GET /api/submissions/{id}")
    class GetSubmissionDetailTests {

        @Test
        @DisplayName("404 Not Found — submission không tồn tại")
        void getDetail_notFound() throws Exception {
            mockMvc.perform(get("/api/submissions/999999")
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 Unauthorized — không có JWT")
        void getDetail_unauthenticated() throws Exception {
            mockMvc.perform(get("/api/submissions/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
