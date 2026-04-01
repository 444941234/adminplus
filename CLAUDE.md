# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AdminPlus is a full-stack RBAC (Role-Based Access Control) management system with:
- **Backend**: Spring Boot 3.5 + JDK 21 + PostgreSQL + Redis
- **Frontend**: Vue 3.5 + BigModel UI + Vite 6 + Pinia

## Build & Development Commands

### Backend (Maven)
```bash
cd backend
mvn spring-boot:run          # Run in development mode
mvn clean package -DskipTests  # Build JAR
mvn test                     # Run all tests
mvn test -Dtest=ClassName    # Run specific test class
```

### Frontend (npm)
```bash
cd frontend
npm install                  # Install dependencies
npm run dev                  # Development server at http://localhost:5173
npm run build                # Production build
npm run lint                 # Run ESLint
npm run test                 # Run Vitest tests
```

### Docker Deployment
```bash
docker-compose up -d         # Start all services (PostgreSQL, Redis, Backend, Frontend)
docker-compose down          # Stop all services
```

## Architecture

### Backend Structure
```
backend/src/main/java/com/adminplus/
├── controller/        # REST API endpoints with @PreAuthorize
├── service/           # Business logic, @Transactional
├── repository/        # JPA repositories
├── pojo/
│   ├── entity/        # JPA entities (use Lombok @Data)
│   └── dto/           # DTOs (use Java record types)
├── common/
│   ├── config/        # Spring configuration
│   ├── security/      # JWT, UserDetailsService
│   ├── exception/     # GlobalExceptionHandler, BizException
│   └── filter/        # XSS filter, rate limiting
└── utils/             # Utility classes
```

### Frontend Structure
```
frontend/src/
├── api/               # Axios API calls
├── stores/            # Pinia stores (Setup Store syntax)
├── router/            # Vue Router with dynamic route loading
├── views/             # Page components
├── layout/            # Layout components
├── components/        # Shared components
├── directives/        # Custom directives (v-auth)
├── composables/       # Vue composables
└── utils/             # Utility functions

frontend/packages/ui-vue/src/components/bigmodel/
├── button/            # BmButton component
├── card/              # BmCard component
├── data/              # BmTable, BmPagination components
├── feedback/          # BmModal, BmToast, BmConfirm components
├── form/              # BmInput, BmSelect, BmCheckbox, BmRadio, BmSwitch components
├── layout/            # BmLayout, BmSidebar, BmHeader components
└── other/             # BmIcon, BmAvatar, BmBadge components
```

## Key Patterns

### Backend Patterns
- **DTOs**: Use Java `record` types for request/response objects
- **Entities**: Use Lombok `@Data`, extend `BaseEntity` for auditing
- **Services**: Annotate with `@Transactional`, return DTOs not entities
- **Controllers**: Use `@PreAuthorize` for permission checks
- **API Response**: All responses wrapped in `ApiResponse<T>` with code, message, data
- **Caching**: Spring Cache with Redis backend

### Frontend Patterns
- **Components**: Use `<script setup>` syntax
- **State**: Pinia stores with Composition API (Setup Store)
- **Permissions**: Use `v-auth` directive or `userStore.hasPermission()`
- **Routes**: Dynamic routes loaded from backend menu API
- **UI Library**: BigModel components (Bm*) from `@adminplus/ui-vue`

### Vue 3 Component Development Best Practices

#### ⚠️ Avoid Duplicate API Calls Pattern

**DO NOT** use `watch(..., {immediate: true})` with `onMounted` for the same function:

```vue
<script setup>
// ❌ WRONG - This causes duplicate requests and "canceled" errors
watch(() => props.id, () => {
  fetchData()
}, { immediate: true })

onMounted(() => {
  fetchData()  // This runs AGAIN!
})
</script>
```

**CORRECT** - Use only `watch` with `immediate: true`:

```vue
<script setup>
// ✅ CORRECT - Runs once on mount and when props.id changes
watch(() => props.id, () => {
  fetchData()
}, { immediate: true })
// No onMounted needed!
</script>
```

**Why?** The `immediate: true` option executes the watcher callback immediately when the component is created, before the DOM is mounted. Adding `onMounted` causes the function to run twice, triggering the request deduplication logic and canceling the second request.

#### Request Deduplication

The project has automatic request deduplication enabled in `useApiInterceptors.ts`. When two identical requests are made simultaneously:
- The first request proceeds normally
- The second request is **automatically cancelled** and shows a "canceled" toast error

**Common scenarios causing duplicate requests:**
1. Multiple components fetching the same data simultaneously
2. `watch` + `onMounted` pattern (see above)
3. Parent and child components both fetching data on mount

**Solutions:**
- Use events to share data between components (e.g., `@loaded` event)
- Use Pinia stores for shared state
- Design components to receive data via props rather than fetching independently

#### Dialog Accessibility

All `DialogContent` components must include a `DialogDescription` for accessibility:

```vue
<Dialog v-model:open="dialogOpen">
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Title</DialogTitle>
      <DialogDescription>
        Brief description of what this dialog does
      </DialogDescription>
    </DialogHeader>
    <!-- Content -->
  </DialogContent>
</Dialog>
```

## BigModel Components

The frontend uses a custom BigModel component library matching Zhipu AI design style. Import components from `@adminplus/ui-vue`:

```vue
<script setup>
import { BmButton, BmCard, BmInput, BmTable, BmModal } from '@adminplus/ui-vue';
</script>
```

### Available Components
| Category | Components |
|----------|------------|
| Layout | BmLayout, BmSidebar, BmHeader |
| Form | BmInput, BmSelect, BmCheckbox, BmRadio, BmRadioGroup, BmSwitch |
| Data | BmTable, BmPagination |
| Feedback | BmModal, BmToast, BmConfirm |
| Other | BmButton, BmCard, BmIcon, BmAvatar, BmBadge |

### Usage Examples
```vue
<!-- Button -->
<BmButton type="primary" @click="handleClick">Submit</BmButton>

<!-- Card -->
<BmCard title="Title" shadow="small">Content</BmCard>

<!-- Input -->
<BmInput v-model="value" placeholder="Enter text" clearable />

<!-- Toast -->
import { useToast } from '@adminplus/ui-vue';
const toast = useToast();
toast.success('Operation successful');
```

## Permission System
- Backend: `@PreAuthorize("hasAuthority('resource:action')")`
- Frontend: `v-auth="'resource:action'"` directive
- Permission format: `{resource}:{action}` (e.g., `user:add`, `role:edit`)

## Workflow Module Special Considerations

### Node List Data Flow

The workflow designer has two components that need node data:
- `WorkflowVisualizer` (flow diagram) - Fetches nodes independently
- `WorkflowDesigner` (node list) - Should NOT fetch nodes independently

**Correct pattern**: Share data via events

```vue
<!-- WorkflowVisualizer.vue -->
<script setup>
const emit = defineEmits<{
  (e: 'loaded', definition: WorkflowDefinition): void
}>()

async function loadWorkflowDefinition() {
  const [defRes, nodesRes] = await Promise.all([
    getWorkflowDefinition(props.definitionId),
    getWorkflowNodes(props.definitionId)
  ])
  definition.value = {
    id: defRes.data.id,
    name: defRes.data.definitionName,
    nodes: mapWorkflowNodes(nodesRes.data)  // Keep original field names!
  }
  emit('loaded', definition.value)
}
</script>

<!-- WorkflowDesigner.vue -->
<template>
  <WorkflowVisualizer
    :definition-id="selectedDefinition.id"
    @loaded="handleWorkflowLoaded"
  />
</template>

<script setup>
const handleWorkflowLoaded = (workflowDef) => {
  nodes.value = workflowDef.nodes  // Reuse fetched data
}
</script>
```

**Important**: Keep field names consistent with backend API response (`nodeName`, `nodeCode`, `nodeOrder`, not `name`, `code`, `order`).

### Workflow Permission Constants

All workflow-related permissions are defined in `SecurityConstants.java`:
- `WORKFLOW_CREATE`, `WORKFLOW_UPDATE`, `WORKFLOW_DELETE` - Workflow instance operations
- `WORKFLOW_DEFINITION_CREATE`, `WORKFLOW_DEFINITION_UPDATE`, `WORKFLOW_DEFINITION_DELETE` - Definition operations
- `WORKFLOW_CC_READ`, `WORKFLOW_CC_LIST` - CC (carbon copy) operations
- `WORKFLOW_URGE_READ`, `WORKFLOW_URGE_LIST` - Urge (reminder) operations

## API Endpoints
- Base URL: `http://localhost:8081/api`
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
- Health Check: `http://localhost:8081/api/actuator/health`

## Default Credentials
- Username: `admin`
- Password: `admin123`

## Required Services
- PostgreSQL on port 5432
- Redis on port 6379

## Testing
- Backend: JUnit 5 + Mockito + MockMvc, H2 for test database
- Frontend: Vitest + Vue Test Utils

## Workflow State Machine

AdminPlus uses Spring State Machine (v4.0.0) for workflow state management.

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Spring State Machine                   │
│                                                          │
│  States: DRAFT, RUNNING, APPROVED, REJECTED, CANCELLED  │
│  Events: SUBMIT, APPROVE, REJECT, CANCEL, ROLLBACK      │
│                                                          │
│  Extended State:                                         │
│  - currentNodeId: Current workflow node ID              │
│  - previousNodeId: Previous node ID (for rollback)      │
│  - rollbackHistory: List of rollback operations         │
└─────────────────────────────────────────────────────────┘
```

### Key Components

**Backend:**
- `WorkflowState.java` - State enum (DRAFT, RUNNING, APPROVED, REJECTED, CANCELLED)
- `WorkflowEvent.java` - Event enum (SUBMIT, APPROVE, REJECT, CANCEL, ROLLBACK)
- `StateMachineConfig.java` - State machine configuration with transitions, guards, and actions
- `WorkflowStateMachineService.java` - Service interface for state operations
- `WorkflowStateMachinePersister.java` - Database persistence for state machine context
- `StateMachineEntity.java` - JPA entity for state machine persistence
- `StateMachineRepository.java` - Repository for state machine entities

**Frontend:**
- `WorkflowVisualizer.vue` - Interactive workflow flow visualization component
- Uses @vue-flow for diagram rendering
- Displays nodes, edges, and state transitions visually

### State Transitions

| From State | Event | To State | Description |
|------------|-------|----------|-------------|
| DRAFT | SUBMIT | RUNNING | Initiator submits workflow |
| RUNNING | APPROVE | RUNNING | Move to next approval node |
| RUNNING | APPROVE | APPROVED | Final approval completes workflow |
| RUNNING | REJECT | REJECTED | Approver rejects workflow |
| RUNNING | CANCEL | CANCELLED | Initiator cancels workflow |
| RUNNING | ROLLBACK | RUNNING | Return to previous node |
| RUNNING | ROLLBACK | DRAFT | Return to draft state |

### Usage Example

```java
// Start state machine
StateMachine<WorkflowState, WorkflowEvent> sm =
    stateMachineFactory.getStateMachine(instanceId);
sm.start();

// Submit workflow
sm.sendEvent(WorkflowEvent.SUBMIT);

// Approve workflow
sm.getExtendedState().getVariables().put("approverId", approverId);
sm.getExtendedState().getVariables().put("comment", "Approved");
sm.sendEvent(WorkflowEvent.APPROVE);

// Reject workflow
sm.getExtendedState().getVariables().put("rejectReason", "Incomplete");
sm.sendEvent(WorkflowEvent.REJECT);
```

### Database Schema

```sql
-- State machine persistence table
CREATE TABLE spring_state_machine_context (
    id VARCHAR(100) PRIMARY KEY,
    machine_id VARCHAR(100) UNIQUE NOT NULL,
    state VARCHAR(50) NOT NULL,
    extended_state JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- Workflow instance with state machine support
ALTER TABLE workflow_instance
ADD COLUMN state_machine_id VARCHAR(100);
```

### Testing

Integration tests: `WorkflowStateMachineIntegrationTest.java`
- State persistence and recovery
- State transitions
- Rollback scenarios
- Extended state management

Run integration tests (requires PostgreSQL):
```bash
cd backend
mvn test -Dtest=WorkflowStateMachineIntegrationTest
```

### Documentation

See `docs/workflow/statemachine-guide.md` for comprehensive workflow state machine documentation including:
- Architecture overview
- Configuration details
- Usage examples
- Guards and actions
- Persistence mechanism
- Testing strategies
- Troubleshooting guide