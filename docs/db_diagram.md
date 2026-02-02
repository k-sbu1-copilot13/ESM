# Database Diagram

Dưới đây là sơ đồ thực thể - quan hệ (ER Diagram) đã được cập nhật dựa trên thay đổi trong `V1__init_schema.sql` (loại bỏ bảng `master_components`).

## Sơ đồ Mermaid

```mermaid
erDiagram
    users {
        bigint id PK
        varchar username
        varchar password
        varchar full_name
        varchar role
        varchar status
    }

    form_templates {
        bigint id PK
        varchar title
        text description
        timestamp created_at
        boolean is_active
    }

    template_fields {
        bigint id PK
        bigint template_id FK
        varchar component_type "TEXT_SHORT, DATE_PICKER, etc."
        varchar label
        boolean is_required
        integer display_order
    }

    workflow_configs {
        bigint id PK
        bigint template_id FK
        bigint manager_id FK
        integer step_order
    }

    submissions {
        bigint id PK
        bigint template_id FK
        bigint employee_id FK
        jsonb form_data
        varchar status
        integer current_step
        timestamp created_at
    }

    approval_logs {
        bigint id PK
        bigint submission_id FK
        bigint manager_id FK
        varchar action
        text comment
        integer at_step
        timestamp created_at
    }

    form_templates ||--o{ template_fields : "contains"
    form_templates ||--o{ workflow_configs : "defined by"
    users ||--o{ workflow_configs : "acts as manager"
    form_templates ||--o{ submissions : "has"
    users ||--o{ submissions : "submits"
    submissions ||--o{ approval_logs : "tracked in"
    users ||--o{ approval_logs : "approves/rejects"
```

## Mô tả chi tiết các bảng

| Bảng | Mô tả |
| :--- | :--- |
| `users` | Lưu thông tin người dùng và vai trò (ADMIN, MANAGER, EMPLOYEE). |
| `form_templates` | Chứa thông tin về tiêu đề và mô tả của các loại đơn. |
| `template_fields` | Các trường nhập liệu thuộc về một form template. Trường `component_type` lưu loại input trực tiếp (không qua bảng trung gian). |
| `workflow_configs` | Cấu hình luồng phê duyệt (các bước duyệt và người duyệt tương ứng). |
| `submissions` | Dữ liệu thực tế của các đơn đã được nhân viên nộp. |
| `approval_logs` | Lịch sử phê duyệt từng bước của các cấp quản lý. |
