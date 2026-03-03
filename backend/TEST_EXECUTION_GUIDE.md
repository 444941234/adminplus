# Test Execution Guide for Workflow Approval Module

## Quick Start

### Prerequisites
- JDK 21+
- Maven 3.9+
- H2 Database (included in test dependencies)

### Run All Tests
```bash
cd backend
mvn clean test
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage Report
Open `target/site/jacoco/index.html` in a browser

## Test Categories

### 1. Unit Tests Only
```bash
# Run all unit tests
mvn test -Dtest=*ServiceTest

# Specific service tests
mvn test -Dtest=WorkflowDefinitionServiceTest
mvn test -Dtest=WorkflowInstanceServiceTest
mvn test -Dtest=ApprovalStatusTransitionTest
```

### 2. Integration Tests Only
```bash
mvn test -Dtest=WorkflowIntegrationTest
```

### 3. Security Tests Only
```bash
mvn test -Dtest=WorkflowSecurityTest
```

### 4. Run Specific Test Method
```bash
mvn test -Dtest=WorkflowInstanceServiceTest#shouldCreateDraftSuccessfully
```

### 5. Run Tests Matching Pattern
```bash
# All approval-related tests
mvn test -Dtest=*Approval*

# All security tests
mvn test -Dtest=*Security*
```

## Test Execution Profiles

### Default Profile (Unit Tests)
```bash
mvn test
```

### Integration Test Profile
```bash
mvn test -Dspring.profiles.active=test
```

### With Debug Output
```bash
mvn test -X
```

### With SQL Logging
```bash
mvn test -Dlogging.level.org.hibernate.SQL=DEBUG
```

## Parallel Execution

### Run Tests in Parallel
```bash
mvn test -DforkCount=4 -DreuseForks=false
```

### Run Test Classes in Parallel
```bash
mvn test -Djunit.jupiter.execution.parallel.enabled=true \
        -Djunit.jupiter.execution.parallel.mode.default=concurrent
```

## Test Reports

### Generate Surefire Report
```bash
mvn surefire-report:report
# Report at: target/site/surefire-report.html
```

### Generate JaCoCo Coverage Report
```bash
mvn jacoco:report
# Report at: target/site/jacoco/index.html
```

### Generate All Reports
```bash
mvn clean test jacoco:report surefire-report:report
```

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run tests
  run: mvn clean test

- name: Generate coverage
  run: mvn jacoco:report

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    files: target/site/jacoco/jacoco.xml
```

### Jenkins Pipeline Example
```groovy
stage('Test') {
    steps {
        sh 'mvn clean test'
    }
    post {
        always {
            junit 'target/surefire-reports/*.xml'
            jacoco execPattern: 'target/jacoco.exec'
        }
    }
}
```

## Troubleshooting

### Tests Fail with "No qualifying bean"
**Problem**: Spring context not loading properly
**Solution**: Check test configuration and component scanning

### Tests Fail with "SecurityContext not available"
**Problem**: Security context not mocked
**Solution**: Add `@RunWith(MockitoExtension.class)` and mock security context

### Integration Tests Fail with "Table not found"
**Problem**: Database schema not created
**Solution**: Verify `spring.jpa.hibernate.ddl-auto=create-drop` in test properties

### Tests Pass Locally but Fail in CI
**Problem**: Environment differences
**Solution**: Use Docker for consistent environment, check system properties

### Slow Test Execution
**Problem**: Tests running sequentially
**Solution**: Enable parallel execution with proper test isolation

## Test Data Management

### Clean Test Database
```bash
mvn clean test -Dspring.jpa.hibernate.ddl-auto=create-drop
```

### Use Testcontainers for Real Database
```bash
# Add dependency to pom.xml
mvn test -Dspring.profiles.active=testcontainers
```

## Performance Testing

### Run Tests with Performance Metrics
```bash
mvn test -Dperf.enabled=true
```

### Generate Performance Report
```bash
mvn test -Djunit.jupiter.execution.parallel.enabled=true \
        -Djunit.jupiter.execution.parallel.config.strategy=fixed \
        -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
```

## Best Practices

1. **Always run tests before committing**
   ```bash
   mvn clean test
   ```

2. **Check coverage before pushing**
   ```bash
   mvn test jacoco:report
   ```

3. **Run specific tests during development**
   ```bash
   mvn test -Dtest=WorkflowInstanceServiceTest#shouldApproveWorkflowSuccessfully
   ```

4. **Use debug mode when investigating failures**
   ```bash
   mvn test -X -Dtest=FailingTest
   ```

5. **Clean before full test runs**
   ```bash
   mvn clean test
   ```

## Test Results Interpretation

### Success Output
```
[INFO] Tests run: 750, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Failure Output
```
[ERROR] Tests run: 750, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failure details in target/surefire-reports
```

### Coverage Report
```
[INFO] Coverage summary:
[INFO]   - Line coverage: 85.2%
[INFO]   - Branch coverage: 82.1%
[INFO]   - Method coverage: 92.5%
[INFO]   - Class coverage: 95.0%
```

## Automated Testing Workflow

### Development Workflow
1. Write failing test (RED)
2. Implement minimal code to pass (GREEN)
3. Run tests: `mvn test -Dtest=NewFeatureTest`
4. Refactor and verify tests still pass
5. Check coverage: `mvn jacoco:report`

### Pre-commit Workflow
```bash
# Run all tests
mvn clean test

# Generate coverage
mvn jacoco:report

# Verify coverage thresholds
mvn jacoco:check
```

### Pre-push Workflow
```bash
# Full test suite
mvn clean test

# Integration tests
mvn test -Dtest=*IntegrationTest

# Security tests
mvn test -Dtest=*SecurityTest

# Verify all pass
mvn verify
```

## Test Maintenance

### Regularly Update Tests
- Review and update tests when changing business logic
- Add new tests for new features
- Remove obsolete tests
- Update test data as needed

### Test Code Quality
- Keep tests simple and focused
- Use descriptive test names
- Follow AAA pattern (Arrange-Act-Assert)
- Avoid test interdependencies
- Use proper assertions

## Resources

- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Maven JaCoCo Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
