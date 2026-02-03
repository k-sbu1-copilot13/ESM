-- 1. Bảng Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED'))
);

-- 2. Bảng Form Templates (Loại đơn)
CREATE TABLE form_templates (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. Bảng Template Fields (Các trường trong form)
CREATE TABLE template_fields (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES form_templates(id),
    component_type VARCHAR(50) NOT NULL, 
    label VARCHAR(200) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    display_order INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- 4. Bảng Workflow Configs (Cấu hình người duyệt)
CREATE TABLE workflow_configs (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES form_templates(id),
    manager_id BIGINT NOT NULL REFERENCES users(id),
    step_order INTEGER NOT NULL
);

-- 5. Bảng Submissions (Đơn đã nộp)
CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES form_templates(id),
    employee_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED')),
    current_step INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reset_at TIMESTAMP
);

-- 6. Bảng Submission Values (Mô hình EAV)
CREATE TABLE submission_values (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    field_id BIGINT NOT NULL REFERENCES template_fields(id),
    field_value TEXT, -- Lưu mọi thứ dạng chuỗi
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Bảng Approval Logs (Lịch sử duyệt)
CREATE TABLE approval_logs (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submissions(id), 
    manager_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(20) NOT NULL CHECK (action IN ('APPROVE', 'REJECT')),
    comment TEXT,
    at_step INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);