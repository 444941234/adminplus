# Notification System Documentation

## Overview

The AdminPlus notification system provides a comprehensive in-app messaging capability for workflow events, system alerts, and user notifications.

## Architecture

### Backend Components

```
backend/src/main/java/com/adminplus/
├── controller/
│   └── NotificationController.java       # REST API endpoints
├── service/
│   └── NotificationService.java          # Business logic
├── repository/
│   └── NotificationRepository.java       # Data access
├── pojo/
│   ├── entity/
│   │   └── NotificationEntity.java       # JPA entity
│   └── dto/
│       ├── req/
│       │   └── NotificationSendReq.java  # Request DTO
│       └── resp/
│           └── NotificationResp.java     # Response DTO
└── statemachine/
    └── actions/
        └── NotifyAction.java             # State machine integration
```

### Database Schema

```sql
CREATE TABLE sys_notification (
    id VARCHAR(100) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,           -- Notification type
    recipient_id VARCHAR(100) NOT NULL,   -- Recipient user ID
    title VARCHAR(200) NOT NULL,          -- Notification title
    content TEXT,                         -- Notification content
    related_id VARCHAR(100),              -- Related business ID
    related_type VARCHAR(50),             -- Related business type
    status INT DEFAULT 0 NOT NULL,        -- 0=unread, 1=read
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_notification_recipient ON sys_notification(recipient_id);
CREATE INDEX idx_notification_status ON sys_notification(status);
CREATE INDEX idx_notification_type ON sys_notification(type);
CREATE INDEX idx_notification_create_time ON sys_notification(create_time);
```

## API Endpoints

| Method | Endpoint | Description | Permission |
|--------|----------|-------------|------------|
| POST | `/api/v1/notifications` | Send notification | `notification:send` |
| POST | `/api/v1/notifications/batch` | Batch send | `notification:send` |
| GET | `/api/v1/notifications` | Get my notifications | None (authenticated) |
| GET | `/api/v1/notifications/unread-count` | Get unread count | None (authenticated) |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read | None (authenticated) |
| PUT | `/api/v1/notifications/read-all` | Mark all as read | None (authenticated) |
| DELETE | `/api/v1/notifications/{id}` | Delete notification | None (authenticated) |

## Notification Types

| Type | Description | Trigger |
|------|-------------|---------|
| `workflow_approve` | Approval passed | Workflow state transition |
| `workflow_reject` | Approval rejected | Workflow state transition |
| `workflow_submit` | Workflow submitted | Workflow submission |
| `workflow_cancel` | Workflow cancelled | Workflow cancellation |
| `workflow_rollback` | Workflow rolled back | Workflow rollback |
| `workflow_cc` | Carbon copy | CC recipient added |
| `workflow_urge` | Reminder sent | Urge action |

## Usage Examples

### Backend: Sending a Notification

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final NotificationService notificationService;

    public void notifyUser(String userId, String message) {
        NotificationSendReq req = new NotificationSendReq();
        req.setType("workflow_approve");
        req.setRecipientId(userId);
        req.setTitle("审批通过");
        req.setContent("您的申请已通过审批");
        req.setRelatedId(someBusinessId);
        req.setRelatedType("workflow");

        notificationService.sendNotification(req);
    }
}
```

### Backend: Workflow Integration

The `NotifyAction` automatically sends notifications during workflow state transitions:

```java
@Bean
public NotifyAction notifyAction(NotificationService notificationService) {
    return new NotifyAction(notificationService);
}
```

### Frontend: Fetching Notifications

```typescript
import { getMyNotifications, getUnreadCount, markAsRead } from '@/api'

// Get notifications
const { data } = await getMyNotifications({ status: 0, page: 0, size: 20 })

// Get unread count
const { data: count } = await getUnreadCount()

// Mark as read
await markAsRead(notificationId)
```

## Testing

### Unit Tests

```bash
cd backend
mvn test -Dtest=NotificationServiceTest
```

### Test Coverage

| Test Class | Tests | Coverage |
|------------|-------|----------|
| `NotificationServiceTest.java` | 8 | sendNotification, sendBatchNotification, markAsRead, markAllAsRead, getUnreadCount, deleteNotification |

## Security

- Users can only access their own notifications (enforced in service layer)
- Sending notifications requires `notification:send` permission
- Recipient ID validation prevents unauthorized notifications

## Performance

- Indexed queries on recipient_id, status, and create_time
- Batch operations for bulk sends
- Efficient pagination for large notification lists

## Future Enhancements

- Email notifications
- SMS notifications
- Push notifications (WebSocket)
- Notification preferences per user
- Notification templates
- Scheduled notifications
