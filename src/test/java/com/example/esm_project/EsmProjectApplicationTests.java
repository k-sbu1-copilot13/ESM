package com.example.esm_project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Dùng H2 thay PostgreSQL — không cần DB thật để chạy test
class EsmProjectApplicationTests {

	@Test
	void contextLoads() {
		// Smoke test: verify toàn bộ Spring context load thành công
		// Nếu có lỗi config, bean thiếu, ... → test này sẽ fail sớm
	}

}
