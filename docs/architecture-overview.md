# Architecture Overview - Dynamic Form System

## System Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        A[Admin UI] -->|Manage Templates| API
        B[Employee UI] -->|Submit Forms| API
        C[Manager UI] -->|Approve/Reject| API
    end
    
    subgraph "API Layer - REST Controllers"
        API[Controllers]
        API --> TC[FormTemplateController]
        API --> FC[TemplateFieldController]
        API --> SC[SubmissionController]
        API --> AC[ApprovalController]
    end
    
    subgraph "Service Layer"
        TC & FC --> TS[FormTemplateService]
        TC & FC --> FS[TemplateFieldService]
        SC --> SS[SubmissionService]
        SS --> CVS[ComponentValidationService]
        AC --> AS[ApprovalService]
        AS --> WS[WorkflowService]
    end
    
    subgraph "Repository Layer - JPA"
        TS --> TR[FormTemplateRepository]
        FS --> FR[TemplateFieldRepository]
        SS --> SR[SubmissionRepository]
        AS --> WR[WorkflowConfigRepository]
        AS --> AR[ApprovalLogRepository]
    end
    
    subgraph "Database"
        TR --> DB[(PostgreSQL)]
        FR --> DB
        SR --> DB
        WR --> DB
        AR --> DB
    end
    
    style API fill:#e1f5ff
    style DB fill:#ffe1e1
```

## Component Types

Hệ thống hỗ trợ **7 loại component**:

1. **TEXT_SHORT** - Text input ngắn
2. **TEXT_AREA** - Textarea nhiều dòng
3. **NUMBER** - Input số
4. **DATE_PICKER** - Chọn ngày
5. **TIME_PICKER** - Chọn giờ
6. **SELECT_BOX** - Dropdown chọn 1 giá trị
7. **CHECKBOX** - Checkbox chọn nhiều giá trị

## Database Schema

```mermaid
erDiagram
    USERS ||--o{ SUBMISSIONS : creates
    USERS ||--o{ WORKFLOW_CONFIGS : "approves as"
    USERS ||--o{ APPROVAL_LOGS : approves
    
    FORM_TEMPLATES ||--o{ TEMPLATE_FIELDS : contains
    FORM_TEMPLATES ||--o{ WORKFLOW_CONFIGS : has
    FORM_TEMPLATES ||--o{ SUBMISSIONS : "based on"
    
    SUBMISSIONS ||--o{ APPROVAL_LOGS : has
    
    USERS {
        bigint id PK
        varchar username UK
        varchar password
        varchar full_name
        varchar role
        varchar status
    }
    
    FORM_TEMPLATES {
        bigint id PK
        varchar title
        text description
        timestamp created_at
        boolean is_active
    }
    
    TEMPLATE_FIELDS {
        bigint id PK
        bigint template_id FK
        varchar component_type
        varchar label
        boolean is_required
        int display_order
        jsonb field_config
    }
    
    WORKFLOW_CONFIGS {
        bigint id PK
        bigint template_id FK
        bigint manager_id FK
        int step_order
    }
    
    SUBMISSIONS {
        bigint id PK
        bigint template_id FK
        bigint employee_id FK
        jsonb form_data
        varchar status
        int current_step
        timestamp created_at
    }
    
    APPROVAL_LOGS {
        bigint id PK
        bigint submission_id FK
        bigint manager_id FK
        varchar action
        text comment
        int at_step
        timestamp created_at
    }
```

## Data Flow: Admin Creates Template

```mermaid
sequenceDiagram
    participant Admin
    participant Controller
    participant TemplateService
    participant FieldService
    participant DB
    
    Admin->>Controller: POST /api/admin/templates
    Controller->>TemplateService: createTemplate(request)
    TemplateService->>DB: Save FormTemplate
    DB-->>TemplateService: Template ID
    
    loop For each field
        TemplateService->>FieldService: createField(templateId, fieldDef)
        FieldService->>DB: Save TemplateField
    end
    
    TemplateService-->>Controller: TemplateResponse
    Controller-->>Admin: 201 Created
```

## Data Flow: Employee Submits Form

```mermaid
sequenceDiagram
    participant Employee
    participant Controller
    participant SubmissionService
    participant ValidationService
    participant WorkflowService
    participant DB
    
    Employee->>Controller: POST /api/submissions
    Controller->>SubmissionService: submitForm(request)
    SubmissionService->>ValidationService: validateFields(data)
    
    alt Invalid Data
        ValidationService-->>Controller: 400 Validation Errors
        Controller-->>Employee: Error Response
    else Valid Data
        ValidationService-->>SubmissionService: OK
        SubmissionService->>DB: Save Submission
        SubmissionService->>WorkflowService: initWorkflow
        WorkflowService->>DB: Set step = 1, status = PENDING
        SubmissionService-->>Controller: SubmissionResponse
        Controller-->>Employee: 201 Created
    end
```

## Data Flow: Manager Approval

```mermaid
sequenceDiagram
    participant Manager
    participant Controller
    participant ApprovalService
    participant WorkflowService
    participant DB
    
    Manager->>Controller: POST /api/approvals/{id}/approve
    Controller->>ApprovalService: approve(id, comment)
    ApprovalService->>DB: Save ApprovalLog
    ApprovalService->>WorkflowService: moveToNextStep
    
    alt Has Next Step
        WorkflowService->>DB: current_step++
        WorkflowService-->>ApprovalService: Next approver notified
    else Final Step
        WorkflowService->>DB: status = APPROVED
        WorkflowService-->>ApprovalService: Employee notified
    end
    
    ApprovalService-->>Controller: Success
    Controller-->>Manager: 200 OK
```

## Component Validation Flow

```mermaid
flowchart TD
    Start[Receive Form Data] --> GetTemplate[Load Template + Fields]
    GetTemplate --> Loop{For Each Field}
    
    Loop --> CheckReq{Required?}
    CheckReq -->|Yes| HasVal{Has Value?}
    HasVal -->|No| ErrReq[Error: Required]
    HasVal -->|Yes| ValidateType
    CheckReq -->|No| HasVal2{Has Value?}
    HasVal2 -->|No| NextField
    HasVal2 -->|Yes| ValidateType
    
    ValidateType --> Switch{Component Type}
    
    Switch -->|TEXT_SHORT| VText[Validate: length, pattern]
    Switch -->|TEXT_AREA| VText
    Switch -->|NUMBER| VNum[Validate: min, max, decimal]
    Switch -->|DATE_PICKER| VDate[Validate: format, range]
    Switch -->|TIME_PICKER| VTime[Validate: format, range]
    Switch -->|SELECT_BOX| VSelect[Validate: value in options]
    Switch -->|CHECKBOX| VCheck[Validate: values in options, min/max]
    
    VText --> OK{Valid?}
    VNum --> OK
    VDate --> OK
    VTime --> OK
    VSelect --> OK
    VCheck --> OK
    
    OK -->|No| ErrType[Error: Type-specific]
    OK -->|Yes| NextField[Next Field]
    
    NextField --> Loop
    Loop -->|Done| Success[All Valid ✓]
    
    ErrReq --> Reject[Return Errors]
    ErrType --> Reject
    Success --> Accept[Accept Submission]
    
    style Start fill:#e1f5ff
    style Success fill:#c8e6c9
    style Reject fill:#ffcdd2
```

## REST API Endpoints

### Admin - Template Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/templates` | Create template | ADMIN |
| PUT | `/api/admin/templates/{id}` | Update template | ADMIN |
| GET | `/api/admin/templates` | List all templates | ADMIN |
| GET | `/api/admin/templates/{id}` | Get template | ADMIN |
| DELETE | `/api/admin/templates/{id}` | Deactivate | ADMIN |

### Admin - Field Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/admin/templates/{id}/fields` | Add field | ADMIN |
| PUT | `/api/admin/templates/fields/{id}` | Update field | ADMIN |
| DELETE | `/api/admin/templates/fields/{id}` | Delete field | ADMIN |
| PUT | `/api/admin/templates/{id}/fields/reorder` | Reorder fields | ADMIN |

### Employee - Submissions

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/templates` | List active templates | ALL |
| GET | `/api/templates/{id}` | Get template detail | ALL |
| POST | `/api/submissions` | Submit form | EMPLOYEE |
| GET | `/api/submissions/my` | My submissions | EMPLOYEE |
| GET | `/api/submissions/{id}` | Submission detail | EMPLOYEE |

### Manager - Approvals

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/approvals/pending` | Pending approvals | MANAGER |
| POST | `/api/approvals/{id}/approve` | Approve | MANAGER |
| POST | `/api/approvals/{id}/reject` | Reject | MANAGER |
| GET | `/api/approvals/history` | Approval history | MANAGER |

## Technology Stack

- **Backend**: Spring Boot 3.4.3, Java 21
- **Database**: PostgreSQL (with JSONB support)
- **ORM**: Spring Data JPA
- **Security**: Spring Security + JWT
- **Validation**: Jakarta Validation
- **Migration**: Flyway
- **API Docs**: Swagger/OpenAPI (Springdoc)

## Key Design Decisions

### 1. JSONB for Dynamic Data

**Tại sao dùng JSONB?**
- Template fields là động, admin có thể thay đổi bất kỳ lúc nào
- Không cần ALTER TABLE mỗi khi thêm field mới
- PostgreSQL JSONB hỗ trợ indexing và query hiệu quả
- Dễ dàng validation và transform data

**Format trong `template_fields.field_config`**:
```json
{
  "placeholder": "Nhập họ tên",
  "minLength": 2,
  "maxLength": 100,
  "pattern": "^[a-zA-Z\\s]+$"
}
```

**Format trong `submissions.form_data`**:
```json
{
  "1": "Nguyễn Văn A",
  "2": "2026-02-10",
  "3": 5,
  "4": "IT",
  "5": ["JAVA", "PYTHON"]
}
```

### 2. Component Type Enum

Dùng enum thay vì string để:
- Type-safety
- IDE autocomplete
- Dễ refactor
- Validation compile-time

### 3. Multi-Step Approval Workflow

- Flexible: Admin định nghĩa số step và người duyệt
- Traceable: Lưu đầy đủ lịch sử trong `approval_logs`
- Scalable: Dễ mở rộng thêm logic (parallel approval, conditional, etc.)

### 4. Display Order

- Cho phép admin sắp xếp thứ tự hiển thị fields
- User experience tốt hơn
- Dễ reorder mà không cần xóa/tạo lại

## Security Considerations

1. **Authorization**
   - Role-based access control (ADMIN, MANAGER, EMPLOYEE)
   - Row-level security: user chỉ xem được đơn của mình
   - Manager chỉ duyệt được đơn trong workflow của mình

2. **Input Validation**
   - Server-side validation bắt buộc
   - Sanitize input để tránh SQL injection, XSS
   - Validate JSONB structure

3. **Audit Trail**
   - Log tất cả CRUD operations trên templates
   - Log tất cả approval actions
   - Timestamp cho mọi thay đổi

## Performance Optimization

### Indexing Strategy

```sql
-- Template fields
CREATE INDEX idx_fields_template ON template_fields(template_id);
CREATE INDEX idx_fields_order ON template_fields(template_id, display_order);

-- Submissions
CREATE INDEX idx_submissions_employee ON submissions(employee_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_submissions_template ON submissions(template_id);
CREATE INDEX idx_form_data ON submissions USING GIN(form_data);

-- Workflow
CREATE INDEX idx_workflow_template ON workflow_configs(template_id, step_order);

-- Approval logs
CREATE INDEX idx_logs_submission ON approval_logs(submission_id);
CREATE INDEX idx_logs_manager ON approval_logs(manager_id);
```

### Caching

```java
@Cacheable("active-templates")
public List<FormTemplate> getActiveTemplates()

@Cacheable("template-fields")
public List<TemplateField> getTemplateFields(Long templateId)
```

Cache invalidation:
- Clear cache khi admin update template
- TTL: 1 hour cho templates

### N+1 Query Prevention

```java
@Query("SELECT t FROM FormTemplate t " +
       "LEFT JOIN FETCH t.fields " +
       "WHERE t.id = :id")
Optional<FormTemplate> findByIdWithFields(Long id);
```

## Future Enhancements

1. **Conditional Logic**: Show/hide fields dựa trên giá trị field khác
2. **Field Dependencies**: Auto-calculate values
3. **Templates from Templates**: Clone/duplicate templates
4. **Version Control**: Track template changes over time
5. **Analytics**: Dashboard showing submission statistics
6. **Notifications**: Email/SMS cho approval workflow
7. **File Attachments**: Thêm component FILE_UPLOAD
8. **Rich Text Editor**: Thêm component RICH_TEXT
9. **Multi-language**: i18n support cho templates
10. **Mobile App**: Native iOS/Android apps
