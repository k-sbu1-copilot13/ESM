-- 1. Thêm cột is_active vào template_fields để hỗ trợ soft delete (đảm bảo đơn cũ vẫn xem được metadata)
ALTER TABLE template_fields ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- 2. Tạo bảng mới submission_values (Mô hình EAV)
CREATE TABLE submission_values (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    field_id BIGINT NOT NULL REFERENCES template_fields(id),
    field_value TEXT, -- Lưu mọi thứ dạng chuỗi
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Xóa cột form_data cũ trong bảng submissions (vì chúng ta chuyển sang bảng mới)
ALTER TABLE submissions DROP COLUMN form_data;
