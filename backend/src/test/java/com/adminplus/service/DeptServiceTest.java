package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.DeptCreateRequest;
import com.adminplus.pojo.dto.response.DeptResponse;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.service.impl.DeptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DeptService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeptService Unit Tests")
class DeptServiceTest {

    @Mock
    private DeptRepository deptRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private DeptServiceImpl deptService;

    private DeptEntity testDept;
    private DeptEntity parentDept;

    @BeforeEach
    void setUp() {
        parentDept = new DeptEntity();
        parentDept.setId("parent-001");
        parentDept.setName("Parent Department");
        parentDept.setCode("PARENT");
        parentDept.setSortOrder(1);
        parentDept.setStatus(1);
        parentDept.setAncestors("0,");

        testDept = new DeptEntity();
        testDept.setId("dept-001");
        testDept.setName("Test Department");
        testDept.setCode("TEST");
        testDept.setLeader("John Doe");
        testDept.setPhone("1234567890");
        testDept.setEmail("test@example.com");
        testDept.setSortOrder(1);
        testDept.setStatus(1);
        testDept.setParent(parentDept);
    }

    @Nested
    @DisplayName("getDeptById Tests")
    class GetDeptByIdTests {

        @Test
        @DisplayName("should return department when exists")
        void getDeptById_WhenExists_ShouldReturnDept() {
            // Given
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));

            // When
            DeptResponse result = deptService.getDeptById("dept-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Department");
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void getDeptById_WhenNotFound_ShouldThrowException() {
            // Given
            when(deptRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deptService.getDeptById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("部门不存在");
        }
    }

    @Nested
    @DisplayName("createDept Tests")
    class CreateDeptTests {

        @Test
        @DisplayName("should create department without parent")
        void createDept_WithoutParent_ShouldCreateDept() {
            // Given
            DeptCreateRequest req = new DeptCreateRequest(
                    null, "Root Department", "ROOT", "Jane Doe",
                    "9876543210", "root@example.com", 1, 1
            );
            when(deptRepository.existsByNameAndDeletedFalse("Root Department")).thenReturn(false);
            DeptEntity newDept = new DeptEntity();
            newDept.setId("new-dept");
            newDept.setName("Root Department");
            when(deptRepository.save(any())).thenReturn(newDept);

            // When
            DeptResponse result = deptService.createDept(req);

            // Then
            assertThat(result).isNotNull();
            verify(deptRepository).save(any(DeptEntity.class));
        }

        @Test
        @DisplayName("should throw exception when department name exists")
        void createDept_WhenNameExists_ShouldThrowException() {
            // Given
            DeptCreateRequest req = new DeptCreateRequest(
                    null, "Existing Dept", "EXISTING", "Jane Doe",
                    "9876543210", "existing@example.com", 1, 1
            );
            when(deptRepository.existsByNameAndDeletedFalse("Existing Dept")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> deptService.createDept(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("部门名称已存在");
        }

        @Test
        @DisplayName("should throw exception when parent not found")
        void createDept_WithNonExistentParent_ShouldThrowException() {
            // Given
            DeptCreateRequest req = new DeptCreateRequest(
                    "non-existent", "Child Department", "CHILD", "Jane Doe",
                    "9876543210", "child@example.com", 1, 1
            );
            when(deptRepository.existsByNameAndDeletedFalse("Child Department")).thenReturn(false);
            when(deptRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deptService.createDept(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("父部门不存在");
        }
    }

    @Nested
    @DisplayName("deleteDept Tests")
    class DeleteDeptTests {

        @Test
        @DisplayName("should delete department without children")
        void deleteDept_WithoutChildren_ShouldDelete() {
            // Given
            testDept.setChildren(List.of());
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));

            // When
            deptService.deleteDept("dept-001");

            // Then
            verify(deptRepository).delete(testDept);
        }

        @Test
        @DisplayName("should throw exception when department has children")
        void deleteDept_WithChildren_ShouldThrowException() {
            // Given
            DeptEntity childDept = new DeptEntity();
            childDept.setId("child-001");
            testDept.setChildren(List.of(childDept));
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));

            // When & Then
            assertThatThrownBy(() -> deptService.deleteDept("dept-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("存在子部门");
        }

        @Test
        @DisplayName("should throw exception when department not found")
        void deleteDept_WhenNotFound_ShouldThrowException() {
            // Given
            when(deptRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deptService.deleteDept("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("部门不存在");
        }
    }

    @Nested
    @DisplayName("getDeptAndChildrenIds Tests")
    class GetDeptAndChildrenIdsTests {

        @Test
        @DisplayName("should return empty list for null deptId")
        void getDeptAndChildrenIds_WithNullId_ShouldReturnEmptyList() {
            // When
            List<String> result = deptService.getDeptAndChildrenIds(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for empty deptId")
        void getDeptAndChildrenIds_WithEmptyId_ShouldReturnEmptyList() {
            // When
            List<String> result = deptService.getDeptAndChildrenIds("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return single id when no children")
        void getDeptAndChildrenIds_WithNoChildren_ShouldReturnSingleId() {
            // Given
            testDept.setAncestors("0,");
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));
            when(deptRepository.findByAncestorsStartingWith("0,dept-001,")).thenReturn(List.of());

            // When
            List<String> result = deptService.getDeptAndChildrenIds("dept-001");

            // Then
            assertThat(result).containsExactly("dept-001");
        }

        @Test
        @DisplayName("should return all child ids recursively")
        void getDeptAndChildrenIds_WithChildren_ShouldReturnAllIds() {
            // Given
            testDept.setAncestors("0,");
            DeptEntity child1 = new DeptEntity();
            child1.setId("child-001");
            DeptEntity child2 = new DeptEntity();
            child2.setId("child-002");
            when(deptRepository.findById("dept-001")).thenReturn(Optional.of(testDept));
            when(deptRepository.findByAncestorsStartingWith("0,dept-001,"))
                    .thenReturn(List.of(child1, child2));

            // When
            List<String> result = deptService.getDeptAndChildrenIds("dept-001");

            // Then
            assertThat(result).containsExactly("dept-001", "child-001", "child-002");
        }
    }
}