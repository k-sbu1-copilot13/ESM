# Test JSON Payloads for Swagger

This document provides ready-to-use JSON payloads for testing the ESM Project APIs via Swagger.

## 1. Tạo Form Template (Admin/Manager)
**Endpoint**: `POST /api/form-templates`

```json
{
  "title": "Đơn xin nghỉ phép",
  "description": "Mẫu đơn dùng để xin nghỉ phép năm hoặc nghỉ không lương",
  "fields": [
    {
      "label": "Lý do nghỉ",
      "componentType": "TEXT_AREA",
      "required": true,
      "displayOrder": 1
    },
    {
      "label": "Số ngày nghỉ",
      "componentType": "NUMBER",
      "required": true,
      "displayOrder": 2
    },
    {
      "label": "Ngày bắt đầu",
      "componentType": "DATE_PICKER",
      "required": true,
      "displayOrder": 3
    }
  ],
  "workflowSteps": [
    {
      "managerId": 3,
      "stepOrder": 2
    },
    {
      "managerId": 4,
      "stepOrder": 1
    }
  ]
}
```

## 2. Employee Nộp Đơn (Employee)
**Endpoint**: `POST /api/submissions/submit`
**Headers**: `X-Employee-Id: [Your_Employee_Id]`

> [!NOTE]
> Giả sử `templateId` là `1` và `fieldId` là `1, 2, 3` tương ứng với các trường trong template vừa tạo.

```json
{
  "templateId": 1,
  "values": {
    "1": "Đi du lịch gia đình",
    "2": "3",
    "3": "2026-03-01"
  }
}
```

## 3. Manager Duyệt (Manager)
**Endpoint**: `POST /api/approvals/submissions/{submissionId}`
**Headers**: `X-Manager-Id: [Your_Manager_Id]`

```json
{
  "action": "APPROVE",
  "comment": "Đã phê duyệt. Chúc bạn đi chơi vui vẻ!"
}
```

## 4. Cập nhật đơn bị Reject (Employee)
**Endpoint**: `POST /api/submissions/submit` (Hoặc `/api/submissions/draft`)
**Headers**: `X-Employee-Id: [Your_Employee_Id]`

> [!NOTE]
> Khi đơn bị `REJECTED`, bạn có thể sửa lại và nộp lại. Cần truyền thêm `id` của đơn cũ.

```json
{
  "id": 2,
  "templateId": 1,
  "values": {
    "1": "Đi du lịch gia đình (Sửa lại lý do)",
    "2": "4",
    "3": "2026-03-01"
  }
}
```

---
> [!TIP]
> Bạn có thể thay đổi `action` thành `REJECT` nếu muốn kiểm tra trường hợp từ chối.
