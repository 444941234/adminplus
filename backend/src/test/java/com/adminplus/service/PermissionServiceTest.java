package com.adminplus.service;

import com.adminplus.pojo.dto.response.PermissionResponse;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PermissionService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private MenuEntity testMenu;
    private RoleEntity testRole;
    private UserRoleEntity testUserRole;

    @BeforeEach
    void setUp() {
        testMenu = new MenuEntity();
        testMenu.setId("menu-001");
        testMenu.setName("User Management");
        testMenu.setPermKey("user:view");
        testMenu.setType(2);

        testRole = new RoleEntity();
        testRole.setId("role-001");
        testRole.setCode("ROLE_ADMIN");

        testUserRole = new UserRoleEntity();
        testUserRole.setId("ur-001");
        testUserRole.setUserId("user-001");
        testUserRole.setRoleId("role-001");
    }

    @Nested
    @DisplayName("getUserPermissions Tests")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("should return permissions for user with roles")
        void getUserPermissions_WithRoles_ShouldReturnPermissions() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of(testUserRole));
            when(roleMenuRepository.findMenuIdsByRoleIds(any(Collection.class))).thenReturn(List.of("menu-001"));
            when(menuRepository.findAllById(any(Set.class))).thenReturn(List.of(testMenu));

            // When
            List<String> result = permissionService.getUserPermissions("user-001");

            // Then
            assertThat(result).contains("user:view");
        }

        @Test
        @DisplayName("should return empty list when user has no roles")
        void getUserPermissions_WithNoRoles_ShouldReturnEmptyList() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of());

            // When
            List<String> result = permissionService.getUserPermissions("user-001");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when roles have no menus")
        void getUserPermissions_WithNoMenus_ShouldReturnEmptyList() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of(testUserRole));
            when(roleMenuRepository.findMenuIdsByRoleIds(any(Collection.class))).thenReturn(List.of());

            // When
            List<String> result = permissionService.getUserPermissions("user-001");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should filter out menus without permKey")
        void getUserPermissions_ShouldFilterMenusWithoutPermKey() {
            // Given
            MenuEntity menuWithoutPerm = new MenuEntity();
            menuWithoutPerm.setId("menu-002");
            menuWithoutPerm.setName("Dashboard");
            menuWithoutPerm.setPermKey(null);

            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of(testUserRole));
            when(roleMenuRepository.findMenuIdsByRoleIds(any(Collection.class))).thenReturn(List.of("menu-001", "menu-002"));
            when(menuRepository.findAllById(any(Set.class))).thenReturn(List.of(testMenu, menuWithoutPerm));

            // When
            List<String> result = permissionService.getUserPermissions("user-001");

            // Then
            assertThat(result).containsExactly("user:view");
        }
    }

    @Nested
    @DisplayName("getUserRoles Tests")
    class GetUserRolesTests {

        @Test
        @DisplayName("should return role codes for user")
        void getUserRoles_ShouldReturnRoleCodes() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of(testUserRole));
            when(roleRepository.findAllById(any())).thenReturn(List.of(testRole));

            // When
            List<String> result = permissionService.getUserRoles("user-001");

            // Then
            assertThat(result).contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should return empty list when user has no roles")
        void getUserRoles_WithNoRoles_ShouldReturnEmptyList() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of());

            // When
            List<String> result = permissionService.getUserRoles("user-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserRoleIds Tests")
    class GetUserRoleIdsTests {

        @Test
        @DisplayName("should return role IDs for user")
        void getUserRoleIds_ShouldReturnRoleIds() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of(testUserRole));

            // When
            List<String> result = permissionService.getUserRoleIds("user-001");

            // Then
            assertThat(result).contains("role-001");
        }

        @Test
        @DisplayName("should return empty list when user has no roles")
        void getUserRoleIds_WithNoRoles_ShouldReturnEmptyList() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of());

            // When
            List<String> result = permissionService.getUserRoleIds("user-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRolePermissions Tests")
    class GetRolePermissionsTests {

        @Test
        @DisplayName("should return permissions for role")
        void getRolePermissions_ShouldReturnPermissions() {
            // Given
            when(roleMenuRepository.findMenuIdByRoleId("role-001")).thenReturn(List.of("menu-001"));
            when(menuRepository.findAllById(any(List.class))).thenReturn(List.of(testMenu));

            // When
            List<String> result = permissionService.getRolePermissions("role-001");

            // Then
            assertThat(result).contains("user:view");
        }

        @Test
        @DisplayName("should return empty list when role has no menus")
        void getRolePermissions_WithNoMenus_ShouldReturnEmptyList() {
            // Given
            when(roleMenuRepository.findMenuIdByRoleId("role-001")).thenReturn(List.of());

            // When
            List<String> result = permissionService.getRolePermissions("role-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllPermissions Tests")
    class GetAllPermissionsTests {

        @Test
        @DisplayName("should return all permissions")
        void getAllPermissions_ShouldReturnPermissions() {
            // Given
            when(menuRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(testMenu));

            // When
            List<PermissionResponse> result = permissionService.getAllPermissions();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).permKey()).isEqualTo("user:view");
        }

        @Test
        @DisplayName("should return empty list when no menus")
        void getAllPermissions_WithNoMenus_ShouldReturnEmptyList() {
            // Given
            when(menuRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());

            // When
            List<PermissionResponse> result = permissionService.getAllPermissions();

            // Then
            assertThat(result).isEmpty();
        }
    }
}