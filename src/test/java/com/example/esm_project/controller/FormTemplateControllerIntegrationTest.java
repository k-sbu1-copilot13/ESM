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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test — FormTemplateController (ADMIN only)
 *
 * Luồng test: MockMvc → JWT Filter → AuthorizationFilter →
 * FormTemplateController → H2 DB
 *
 * Test các scenarios:
 * - ADMIN token → có quyền truy cập
 * - EMPLOYEE token → 403 Forbidden (không có quyền ADMIN)
 * - Không có token → 403 Forbidden
 * - POST /api/admin/form-templates (tạo template, validation error)
 * - GET /api/admin/form-templates (danh sách)
 * - GET /api/admin/form-templates/{id} (detail, không tồn tại)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FormTemplateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        // Tạo ADMIN user
        User admin = new User();
        admin.setUsername("admin_test");
        admin.setPassword(passwordEncoder.encode("adminpass123"));
        admin.setFullName("Admin User");
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        userRepository.save(admin);

        // Tạo EMPLOYEE user (không có quyền ADMIN)
        User employee = new User();
        employee.setUsername("employee_test");
        employee.setPassword(passwordEncoder.encode("emppass123"));
        employee.setFullName("Employee User");
        employee.setRole("EMPLOYEE");
        employee.setStatus("ACTIVE");
        userRepository.save(employee);

        // Lấy token cho cả 2 user
        adminToken = loginAndGetToken("admin_test", "adminpass123");
        employeeToken = loginAndGetToken("employee_test", "emppass123");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);

        String responseJson = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseJson).get("token").asText();
    }

    // =========================================================================
    // Phân quyền — test RBAC (Role-Based Access Control)
    // =========================================================================
    @Nested
    @DisplayName("Phân quyền — ADMIN only endpoints")
    class AuthorizationTests {

        @Test
        @DisplayName("200 OK — ADMIN có quyền truy cập GET /api/admin/form-templates")
        void getAllTemplates_adminAccess() throws Exception {
            mockMvc.perform(get("/api/admin/form-templates")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("403 Forbidden — EMPLOYEE không có quyền ADMIN")
        void getAllTemplates_employeeForbidden() throws Exception {
            mockMvc.perform(get("/api/admin/form-templates")
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("401 Unauthorized — không có JWT token (EntryPoint trả 401)")
        void getAllTemplates_unauthenticated() throws Exception {
            // JwtAuthenticationEntryPoint trả 401 khi request không có token
            // 403 chỉ xảy ra khi đã authenticate nhưng thiếu role
            mockMvc.perform(get("/api/admin/form-templates"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // POST /api/admin/form-templates — tạo template mới
    // =========================================================================
    @Nested
    @DisplayName("POST /api/admin/form-templates")
    class CreateTemplateTests {

        @Test
        @DisplayName("400 Bad Request — thiếu trường bắt buộc (title null)")
        void createTemplate_missingTitle() throws Exception {
            // Gửi body thiếu title → @Valid sẽ reject
            String body = """
                    {
                        "description": "Test description",
                        "fields": []
                    }
                    """;

            mockMvc.perform(post("/api/admin/form-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("403 Forbidden — EMPLOYEE không được tạo template")
        void createTemplate_employeeForbidden() throws Exception {
            String body = """
                    {
                        "title": "Test Template",
                        "description": "Test"
                    }
                    """;

            mockMvc.perform(post("/api/admin/form-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /api/admin/form-templates/{id} — lấy template theo ID
    // =========================================================================
    @Nested
    @DisplayName("GET /api/admin/form-templates/{id}")
    class GetTemplateByIdTests {

        @Test
        @DisplayName("404 Not Found — template không tồn tại")
        void getTemplateById_notFound() throws Exception {
            mockMvc.perform(get("/api/admin/form-templates/999999")
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
