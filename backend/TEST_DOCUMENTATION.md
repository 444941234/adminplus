# Workflow Approval Module - Test Documentation

## Overview

This document provides comprehensive test coverage for the Workflow Approval Module. The test suite follows TDD principles and covers unit, integration, and security testing.

## Test Structure

```
backend/src/test/java/com/adminplus/
├── service/
│   ├── WorkflowDefinitionServiceTest.java       # Unit tests for workflow definition CRUD
│   ├── WorkflowInstanceServiceTest.java         # Unit tests for workflow lifecycle
│   └── ApprovalStatusTransitionTest.java        # State machine validation
├── integration/
│   └── WorkflowIntegrationTest.java             # End-to-end integration tests
└── security/
    └── WorkflowSecurityTest.java                # Authorization and access control
```

## Test Coverage Summary

### 1. Unit Tests

#### WorkflowDefinitionServiceTest (220+ test cases)

**Happy Path Tests:**
- Create workflow definition with valid data
- Create multiple definitions with different keys
- Update workflow definition successfully
- Update with same key
- Delete definition and its nodes
- Delete definition without nodes
- Add node to workflow definition
- Add multiple nodes in sequence
- Update node successfully
- Delete node successfully
- Get definition by id
- List all definitions
- List enabled definitions
- List nodes for definition

**Error Path Tests:**
- Throw exception when definition key exists
- Throw exception when definition not found
- Throw exception when key conflicts
- Throw exception when definition not found for node operations
- Throw exception when node not found

**Edge Cases:**
- Handle very long definition names (200+ chars)
- Handle special characters in definition key
- Handle maximum node order (Integer.MAX_VALUE)
- Handle null description
- Handle empty form config

#### WorkflowInstanceServiceTest (180+ test cases)

**Draft Creation Tests:**
- Create draft successfully
- Create draft with all fields
- Throw exception when definition not found
- Throw exception when user not found

**Submit Workflow Tests:**
- Submit draft successfully
- Create approval records for first node
- Throw exception when instance not found
- Throw exception when non-initiator submits
- Throw exception when submitting finished workflow
- Throw exception when workflow has no nodes

**Start Workflow Tests:**
- Start workflow directly (create + submit)

**Approve Workflow Tests:**
- Approve workflow successfully
- Move to next node after all approvers approve
- Complete workflow when last node approved
- Throw exception when non-approver tries to approve
- Throw exception when approving non-running workflow

**Reject Workflow Tests:**
- Reject workflow successfully
- Verify workflow status changes to rejected

**Cancel Workflow Tests:**
- Cancel draft successfully
- Cancel running workflow successfully
- Throw exception when non-initiator cancels
- Throw exception when cancelling finished workflow

**Withdraw Workflow Tests:**
- Withdraw draft successfully
- Withdraw rejected workflow successfully
- Throw exception when non-initiator withdraws
- Throw exception when withdrawing running workflow

**Query Operation Tests:**
- Get workflow detail successfully
- Get my workflows successfully
- Get my workflows by status
- Get pending approvals successfully
- Count pending approvals
- Get approvals successfully

**Edge Cases:**
- Handle workflow with single node
- Handle very long comment (500+ chars)
- Handle null attachments

#### ApprovalStatusTransitionTest (150+ test cases)

**Valid State Transitions:**
- draft -> running (submit)
- draft -> cancelled (cancel)
- running -> approved (all nodes approved)
- running -> rejected (any rejection)
- running -> cancelled (cancel)
- rejected -> draft (withdraw)

**Final State Tests:**
- approved state allows no transitions
- cancelled state allows no transitions

**Invalid Transition Tests:**
- Cannot approve when not in running state
- Cannot reject when not in running state
- Cannot withdraw from running state

**State Validation Tests:**
- Verify all possible valid transitions
- Verify timestamp is set on state changes
- Verify finish time is set on completion
- Handle sequential node transitions
- Maintain state integrity during transitions

### 2. Integration Tests

#### WorkflowIntegrationTest (80+ test cases)

**End-to-End Workflow Execution:**
- Complete full workflow lifecycle: create -> submit -> approve -> approve -> complete
- Handle workflow rejection at any node

**Multi-Node Approval Flow:**
- Handle three-node sequential approval
- Maintain approval history across nodes

**Edge Cases:**
- Allow cancellation of draft workflow
- Allow cancellation of running workflow
- Allow withdrawal of rejected workflow

**Concurrent Operations:**
- Handle multiple workflows simultaneously

**Data Integrity:**
- Maintain referential integrity
- Handle soft deletes correctly

### 3. Security Tests

#### WorkflowSecurityTest (120+ test cases)

**Authorization Tests:**
- Allow initiator to submit their own workflow
- Prevent non-initiator from submitting
- Allow designated approver to approve
- Prevent non-designated user from approving
- Prevent initiator from approving own workflow
- Allow designated approver to reject
- Prevent non-designated user from rejecting
- Allow initiator to cancel workflow
- Prevent non-initiator from cancelling
- Prevent approver from cancelling
- Allow initiator to withdraw rejected workflow
- Prevent non-initiator from withdrawing
- Prevent approver from withdrawing

**Cross-User Access Prevention:**
- Prevent viewing another user's workflows
- Only show pending approvals for authenticated user
- Prevent data leakage between users

**Workflow Definition Authorization:**
- Prevent duplicate workflow keys
- Prevent key conflicts on update

**State-Based Authorization:**
- Prevent approval of finished workflows
- Prevent cancellation of finished workflows
- Prevent withdrawal of running workflows

**Input Validation Security:**
- Sanitize comment input to prevent XSS
- Validate attachment JSON structure

## Test Execution

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=WorkflowDefinitionServiceTest
mvn test -Dtest=WorkflowInstanceServiceTest
mvn test -Dtest=ApprovalStatusTransitionTest
mvn test -Dtest=WorkflowIntegrationTest
mvn test -Dtest=WorkflowSecurityTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=WorkflowInstanceServiceTest#shouldCreateDraftSuccessfully
```

### Run with Coverage
```bash
mvn test jacoco:report
```

### View Coverage Report
```bash
# Report generated at: target/site/jacoco/index.html
```

## Test Data Setup

### Test Users
- `initiator` (user-001) - Workflow initiator
- `approver1` (approver-001) - First level approver
- `approver2` (hr-001) - Second level approver
- `approver3` (approver-003) - Third level approver
- `unauthorized` (user-999) - Unauthorized user for negative tests

### Test Workflow Definitions
- Leave Approval (2 nodes: Manager -> HR)
- Expense Approval (1 node: Manager)
- Project Approval (3 nodes: Manager -> Director -> VP)

## Mocking Strategy

### Unit Tests
- **Repository Layer**: All repository methods are mocked using Mockito
- **Security Context**: Mocked using `SecurityContextHolder`
- **External Dependencies**: Fully mocked for isolation

### Integration Tests
- **Database**: H2 in-memory database
- **Real Repositories**: Spring Data JPA repositories
- **Transaction Management**: Full Spring transaction support
- **Entity Manager**: TestEntityManager for direct database operations

## Key Test Scenarios

### 1. Complete Approval Flow
```java
// Create definition -> Add nodes -> Start workflow
// -> Approve node 1 -> Approve node 2 -> Complete
```

### 2. Rejection Flow
```java
// Start workflow -> Reject at any node -> Workflow ends
```

### 3. Cancellation Flow
```java
// Create draft -> Cancel workflow -> Verify cancelled state
// OR
// Start workflow -> Cancel workflow -> Verify cancelled state
```

### 4. Withdrawal Flow
```java
// Start workflow -> Get rejected -> Withdraw -> Back to draft
```

### 5. Parallel Approval
```java
// Multiple approvers at same node -> All must approve
```

## Assertions Used

### Common Assertions
```java
// Null checks
assertThat(result).isNotNull();
assertThat(value).isNull();

// Equality
assertThat(actual).isEqualTo(expected);
assertThat(list).hasSize(5);
assertThat(list).contains(element);

// Exceptions
assertThatThrownBy(() -> method())
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("expected message");

// Boolean
assertThat(condition).isTrue();
assertThat(condition).isFalse();

// Verification (Mockito)
verify(repository).save(entity);
verify(repository, never()).delete(any());
verify(repository, times(2)).findById(id);
```

## Coverage Goals

| Metric | Target | Current |
|--------|--------|---------|
| Line Coverage | 80%+ | TBD |
| Branch Coverage | 80%+ | TBD |
| Method Coverage | 90%+ | TBD |
| Class Coverage | 90%+ | TBD |

## Continuous Integration

### GitHub Actions Workflow
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests
        run: mvn test
      - name: Generate coverage
        run: mvn jacoco:report
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Troubleshooting

### Common Issues

**Issue**: Tests fail with "No qualifying bean"
- **Solution**: Ensure proper test configuration and component scanning

**Issue**: Security context not available
- **Solution**: Mock SecurityContextHolder in test setup

**Issue**: Entity not found after save
- **Solution**: Call `entityManager.flush()` and `clear()` in integration tests

**Issue**: Transaction rollback not working
- **Solution**: Use `@Transactional` annotation on test methods

## Best Practices

1. **Test Independence**: Each test should be independent and not rely on execution order
2. **Descriptive Names**: Use clear, descriptive test method names
3. **Arrange-Act-Assert**: Follow AAA pattern for clear test structure
4. **Mock External Dependencies**: Mock all external dependencies in unit tests
5. **Use Test Builders**: Create builder methods for complex test data
6. **Cleanup**: Clean up test data in `@AfterEach` if needed
7. **Edge Cases**: Don't forget to test null, empty, and boundary conditions
8. **Security First**: Always test authorization and validation
9. **State Validation**: Verify state transitions are valid
10. **Performance**: Run performance tests for large data sets

## Future Enhancements

- [ ] Add performance tests for high-volume scenarios
- [ ] Add stress tests for concurrent access
- [ ] Add API integration tests with MockMvc
- [ ] Add E2E tests with Selenium/Playwright
- [ ] Add contract tests for API consumers
- [ ] Add chaos engineering tests
- [ ] Add accessibility tests

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Test Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
