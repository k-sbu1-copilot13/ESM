# ESM Project - Spring Boot REST API

Ứng dụng Spring Boot REST API cho hệ thống Quản lý đơn nộp của nhân viên (Employee Submission Management - ESM).

## 🚀 Hướng dẫn chạy hệ thống (Quick Start)

### 1. Yêu cầu hệ thống
- **Java**: 21
- **Gradle**: 8.x+ (hoặc dùng `./gradlew`)
- **Database**: PostgreSQL 16+
- **Docker & Docker Compose** (nếu chạy qua Docker)

### 2. Cấu hình môi trường (Environment Variables)
Tạo file `.env` hoặc thiết lập các biến môi trường sau:
- `DB_HOST`: Host của database (mặc định: `localhost`)
- `DB_PORT`: Port của database (mặc định: `5433`)
- `DB_NAME`: Tên database (mặc định: `esm_db`)
- `DB_USERNAME`: Username (mặc định: `postgres`)
- `DB_PASSWORD`: Password (mặc định: `postgres`)
- `JWT_SECRET`: Khóa bí mật cho JWT (sử dụng giá trị mặc định trong `application.yml` cho local)

### 3. Chạy bằng Docker (Khuyên dùng)
Hệ thống đã được cấu hình sẵn Docker Compose bao gồm database và ứng dụng.

```bash
# Clone dự án
git clone https://github.com/manhtq99/esm_project.git
cd esm_project

# Khởi chạy hệ thống
docker-compose up -d

# Xem log
docker-compose logs -f

# Bật Database
docker compose up db -d
```

### 4. Chạy trực tiếp qua Gradle (Local)
Nếu bạn muốn chạy trực tiếp trên máy:

1. **Chuẩn bị Database**:
   ```bash
   # Tạo database nếu chưa có
   createdb -h localhost -p 5433 -U postgres esm_db
   ```
2. **Khởi chạy ứng dụng**:
   ```bash
   ./gradlew bootRun
   ```

---

## 🛠 Cấu trúc Database (EAV Model)

Dự án sử dụng mô hình **Entity-Attribute-Value (EAV)** để lưu trữ dữ liệu đơn nộp một cách linh hoạt.

- **users**: Quản lý tài khoản (ADMIN, MANAGER, EMPLOYEE).
- **form_templates**: Định nghĩa các loại đơn.
- **template_fields**: Danh sách các trường động trong từng loại đơn.
- **submissions**: Thông tin chung của đơn đã nộp.
- **submission_values**: Lưu dữ liệu thực tế cho từng trường (theo mô hình EAV).
- **approval_logs**: Lịch sử phê duyệt.

> [!TIP]
> Bạn có thể xem sơ đồ chi tiết tại [docs/db_diagram.md](docs/db_diagram.md).

---

## 📖 API Documentation

Khi ứng dụng đang chạy, bạn có thể truy cập:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Health Check & Monitoring
- **Health Status**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **Info**: [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)

---

## 🔐 Bảo mật
- Mật khẩu được mã hóa bằng **BCrypt**.
- Xác thực qua **JWT Token**.
- Phân quyền theo vai trò (Role-based access control).

