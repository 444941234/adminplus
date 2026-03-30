package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.req.RoleCreateReq;
import com.adminplus.pojo.dto.req.RoleUpdateReq;
import com.adminplus.pojo.dto.resp.RoleResp;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.RoleMenuEntity;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.impl.RoleServiceImpl;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RoleService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private RoleServiceImpl roleService;

    private RoleEntity testRole;

    @BeforeEach
    void setUp() {
        testRole = new RoleEntity();
        testRole.setId("role-001");
        testRole.setCode("ROLE_USER");
        testRole.setName("User");
        testRole.setDescription("Regular user role");
        testRole.setDataScope(1);
        testRole.setStatus(1);
        testRole.setSortOrder(1);
    }

    @Nested
    @DisplayName("getRoleById Tests")
    class GetRoleByIdTests {

        @Test
        @DisplayName("should return role when exists")
        void getRoleById_WhenExists_ShouldReturnRole() {
            // Given
            when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));

            // When
            RoleResp result = roleService.getRoleById("role-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.code()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void getRoleById_WhenNotFound_ShouldThrowException() {
            // Given
            when(roleRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.getRoleById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("getRoleList Tests")
    class GetRoleListTests {

        @Test
        @DisplayName("should return role list")
        void getRoleList_ShouldReturnRoleList() {
            // Given — non-admin user, database-level filter
            when(roleRepository.findByDeletedFalseAndCodeNot("ROLE_ADMIN")).thenReturn(List.of(testRole));

            // When
            List<RoleResp> result = roleService.getRoleList();

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no roles")
        void getRoleList_WhenNoRoles_ShouldReturnEmptyList() {
            // Given
            when(roleRepository.findByDeletedFalseAndCodeNot("ROLE_ADMIN")).thenReturn(List.of());

            // When
            List<RoleResp> result = roleService.getRoleList();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createRole Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("should create role successfully")
        void createRole_ShouldCreateRole() {
            // Given
            RoleCreateReq req = new RoleCreateReq(
                    "ROLE_MANAGER", "Manager", "Manager role", 1, 1, 2
            );
            when(roleRepository.existsByCode("ROLE_MANAGER")).thenReturn(false);
            when(roleRepository.save(any())).thenReturn(testRole);

            // When
            RoleResp result = roleService.createRole(req);

            // Then
            assertThat(result).isNotNull();
            verify(roleRepository).save(any(RoleEntity.class));
        }

        @Test
        @DisplayName("should throw exception when code exists")
        void createRole_WhenCodeExists_ShouldThrowException() {
            // Given
            RoleCreateReq req = new RoleCreateReq(
                    "ROLE_USER", "User", "User role", 1, 1, 1
            );
            when(roleRepository.existsByCode("ROLE_USER")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> roleService.createRole(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色编码已存在");
        }
    }

    @Nested
    @DisplayName("updateRole Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("should update role successfully")
        void updateRole_ShouldUpdateRole() {
            // Given
            RoleUpdateReq req = new RoleUpdateReq(
                    "Updated Role", "Updated description", 2, 1, 2
            );
            when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));
            when(roleRepository.save(any())).thenReturn(testRole);

            // When
            RoleResp result = roleService.updateRole("role-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(roleRepository).save(any(RoleEntity.class));
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void updateRole_WhenNotFound_ShouldThrowException() {
            // Given
            RoleUpdateReq req = new RoleUpdateReq(
                    "Updated Role", "Updated description", 2, 1, 2
            );
            when(roleRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.updateRole("non-existent", req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("deleteRole Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("should delete role successfully")
        void deleteRole_ShouldDeleteRole() {
            // Given
            when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));
            when(userRoleRepository.existsByRoleId("role-001")).thenReturn(false);
            doNothing().when(roleMenuRepository).deleteByRoleId("role-001");
            doNothing().when(roleRepository).delete(testRole);

            // When
            roleService.deleteRole("role-001");

            // Then
            verify(roleMenuRepository).deleteByRoleId("role-001");
            verify(roleRepository).delete(testRole);
        }

        @Test
        @DisplayName("should throw exception when role has users")
        void deleteRole_WhenRoleHasUsers_ShouldThrowException() {
            // Given
            when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));
            when(userRoleRepository.existsByRoleId("role-001")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole("role-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已分配给用户");
        }

        @Test
        @DisplayName("should throw exception when deleting admin role")
        void deleteRole_WhenAdminRole_ShouldThrowException() {
            // Given
            testRole.setCode("ROLE_ADMIN");
            when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));

            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole("role-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("超级管理员");
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void deleteRole_WhenNotFound_ShouldThrowException() {
            // Given
            when(roleRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.deleteRole("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("assignMenus Tests")
    class AssignMenusTests {

        @Test
        @DisplayName("should assign menus successfully")
        void assignMenus_ShouldAssignMenus() {
            // Given
            List<String> menuIds = List.of("menu-001", "menu-002");
            when(roleRepository.findById("role-001")).thenReturn(Optional.of(testRole));
            // Current menu IDs (will be removed)
            when(roleMenuRepository.findMenuIdByRoleId("role-001")).thenReturn(List.of("existing-menu"));
            doNothing().when(roleMenuRepository).deleteByRoleIdAndMenuIdIn("role-001", Set.of("existing-menu"));
            when(roleMenuRepository.saveAll(any())).thenReturn(List.of());

            // When
            roleService.assignMenus("role-001", menuIds);

            // Then
            verify(roleMenuRepository).deleteByRoleIdAndMenuIdIn("role-001", Set.of("existing-menu"));
            verify(roleMenuRepository).saveAll(any());
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void assignMenus_WhenRoleNotFound_ShouldThrowException() {
            // Given
            when(roleRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> roleService.assignMenus("non-existent", List.of()))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("getRoleMenuIds Tests")
    class GetRoleMenuIdsTests {

        @Test
        @DisplayName("should return menu IDs for role")
        void getRoleMenuIds_ShouldReturnMenuIds() {
            // Given
            when(roleRepository.existsById("role-001")).thenReturn(true);
            when(roleMenuRepository.findMenuIdByRoleId("role-001")).thenReturn(List.of("menu-001", "menu-002"));

            // When
            List<String> result = roleService.getRoleMenuIds("role-001");

            // Then
            assertThat(result).containsExactly("menu-001", "menu-002");
        }

        @Test
        @DisplayName("should throw exception when role not found")
        void getRoleMenuIds_WhenRoleNotFound_ShouldThrowException() {
            // Given
            when(roleRepository.existsById("non-existent")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> roleService.getRoleMenuIds("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("角色不存在");
        }
    }
}