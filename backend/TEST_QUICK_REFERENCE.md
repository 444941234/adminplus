# Workflow Approval Module - Test Quick Reference

## Test Files at a Glance

| File | Tests | Focus | Run Command |
|------|-------|-------|-------------|
| `WorkflowDefinitionServiceTest.java` | 220+ | CRUD operations | `mvn test -Dtest=WorkflowDefinitionServiceTest` |
| `WorkflowInstanceServiceTest.java` | 180+ | Workflow lifecycle | `mvn test -Dtest=WorkflowInstanceServiceTest` |
| `ApprovalStatusTransitionTest.java` | 150+ | State machine | `mvn test -Dtest=ApprovalStatusTransitionTest` |
| `WorkflowIntegrationTest.java` | 80+ | End-to-end | `mvn test -Dtest=WorkflowIntegrationTest` |
| `WorkflowSecurityTest.java` | 120+ | Authorization | `mvn test -Dtest=WorkflowSecurityTest` |

## Common Test Commands

```bash
# Run all tests
mvn clean test

# Run with coverage
mvn clean test jacoco:report

# Run specific test class
mvn test -Dtest=WorkflowInstanceServiceTest

# Run specific test method
mvn test -Dtest=WorkflowInstanceServiceTest#shouldApproveWorkflowSuccessfully

# Run all unit tests
mvn test -Dtest=*ServiceTest

# Run integration tests only
mvn test -Dtest=*IntegrationTest

# Run security tests only
mvn test -Dtest=*SecurityTest
```

## Test Structure Template

```java
@Nested
@DisplayName("Feature Name - Category")
class FeatureNameCategory {

    @Test
    @DisplayName("Should do something when condition")
    void shouldDoSomethingWhenCondition() {
        // Given - Setup test data
        mockSecurityContext(USER_ID);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        // When - Execute action
        var result = service.action(id);

        // Then - Verify results
        assertThat(result).isNotNull();
        verify(repository).save(any());
    }
}
```

## Common Mock Setups

### Security Context
```java
private void mockSecurityContext(String userId) {
    when(SecurityContextHolder.getContext().getAuthentication().getName())
            .thenReturn(userId);
}
```

### Repository Mock
```java
when(repository.findById(id)).thenReturn(Optional.of(entity));
when(repository.save(any())).thenReturn(entity);
when(repository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
```

### User Repository
```java
when(userRepository.findById(userId)).thenReturn(Optional.of(user));
```

## Common Assertions

```java
// Null checks
assertThat(result).isNotNull();
assertThat(value).isNull();

// Equality
assertThat(actual).isEqualTo(expected);

// Collections
assertThat(list).hasSize(5);
assertThat(list).contains(element);
assertThat(list).isEmpty();

// Exceptions
assertThatThrownBy(() -> service.method())
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("expected message");

// Booleans
assertThat(condition).isTrue();
assertThat(value).isFalse();

// Verification
verify(repository).save(entity);
verify(repository, never()).delete(any());
verify(repository, times(2)).findById(id);
```

## Test Data Builders

```java
private WorkflowInstanceEntity createTestInstance() {
    WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
    instance.setId("inst-001");
    instance.setDefinitionId("def-001");
    instance.setUserId("user-001");
    instance.setTitle("Test Request");
    instance.setStatus("running");
    return instance;
}

private UserEntity createTestUser(String id, String name) {
    UserEntity user = new UserEntity();
    user.setId(id);
    user.setNickname(name);
    return user;
}
```

## Status Transition Matrix

| From | To | Valid? | Method |
|------|----|----|--------|
| draft | running | Yes | submit() |
| draft | cancelled | Yes | cancel() |
| running | approved | Yes | approve() (last node) |
| running | rejected | Yes | reject() |
| running | cancelled | Yes | cancel() |
| rejected | draft | Yes | withdraw() |
| approved | * | No | - |
| cancelled | * | No | - |

## Authorization Matrix

| Action | Who Can Do It |
|--------|---------------|
| submit | Initiator only |
| approve | Designated approvers only |
| reject | Designated approvers only |
| cancel | Initiator only |
| withdraw | Initiator only (draft/rejected) |

## Test Naming Convention

```
should[ExpectedBehavior]When[StateUnderTest]
should[ExpectedBehavior]When[StateUnderTest]With[Input]
shouldThrowExceptionWhen[InvalidState]
shouldPrevent[Action]When[Unauthorized]
```

Examples:
- `shouldCreateDraftSuccessfully`
- `shouldMoveToNextNodeWhenAllApproversApprove`
- `shouldThrowExceptionWhenNonInitiatorSubmits`
- `shouldPreventNonApproverFromApproving`

## Edge Cases to Test

- Null values
- Empty strings/lists
- Very long strings (200+, 500+ chars)
- Special characters
- Maximum/minimum values
- Invalid state transitions
- Concurrent operations
- Missing relationships

## Security Tests Checklist

- [ ] Only initiator can submit
- [ ] Only designated approver can approve
- [ ] Initiator cannot approve own workflow
- [ ] Only initiator can cancel
- [ ] Only initiator can withdraw
- [ ] Cross-user access prevented
- [ ] Data isolation maintained
- [ ] Input validation works

## Coverage Goals

- Line coverage: 80%+
- Branch coverage: 80%+
- Method coverage: 90%+
- Class coverage: 90%+

## Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| No qualifying bean | Check test configuration |
| SecurityContext error | Mock SecurityContextHolder |
| Table not found | Check ddl-auto setting |
| Tests pass locally, fail in CI | Check environment differences |
| Slow execution | Enable parallel execution |

## Test Best Practices

1. **One assertion per test** (mostly)
2. **Arrange-Act-Assert** pattern
3. **Descriptive test names**
4. **Independent tests** (no order dependency)
5. **Mock external dependencies**
6. **Test edge cases**
7. **Verify security**
8. **Clean up in @AfterEach** if needed

## Resources

- JUnit 5: https://junit.org/junit5/docs/current/user-guide/
- Mockito: https://javadoc.io/doc/org.mockito/mockito-core/latest/
- AssertJ: https://assertj.github.io/doc/
- Spring Test: https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html

## Test Execution Quick Commands

```bash
# Full test suite
mvn clean test

# With coverage
mvn clean test jacoco:report

# Specific test
mvn test -Dtest=WorkflowInstanceServiceTest

# Debug mode
mvn test -X -Dtest=WorkflowInstanceServiceTest

# Parallel execution
mvn test -DforkCount=4

# Skip tests
mvn clean install -DskipTests
```

## CI/CD Integration

```yaml
# GitHub Actions
- name: Run tests
  run: mvn clean test

- name: Generate coverage
  run: mvn jacoco:report

- name: Check coverage
  run: mvn jacoco:check
```

## Remember

- **RED**: Write failing test
- **GREEN**: Make it pass
- **REFACTOR**: Improve code
- **DOCUMENT**: Add comments if needed
- **REPEAT**: For each feature

Happy Testing!
