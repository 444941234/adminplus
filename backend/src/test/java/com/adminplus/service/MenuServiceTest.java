package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.MenuCreateRequest;
import com.adminplus.pojo.dto.response.MenuResponse;
import com.adminplus.pojo.entity.MenuEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleMenuRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.impl.MenuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MenuService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService Unit Tests")
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private LogService logService;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private MenuServiceImpl menuService;

    private MenuEntity testMenu;
    private MenuEntity parentMenu;
    private MenuResponse testMenuResponse;

    @BeforeEach
    void setUp() {
        parentMenu = new MenuEntity();
        parentMenu.setId("parent-001");
        parentMenu.setName("Parent Menu");
        parentMenu.setType(1);
        parentMenu.setPath("/parent");
        parentMenu.setSortOrder(1);
        parentMenu.setVisible(1);
        parentMenu.setStatus(1);
        parentMenu.setAncestors("0,");

        testMenu = new MenuEntity();
        testMenu.setId("menu-001");
        testMenu.setName("Test Menu");
        testMenu.setType(2);
        testMenu.setPath("/test");
        testMenu.setComponent("TestComponent");
        testMenu.setPermKey("test:view");
        testMenu.setSortOrder(1);
        testMenu.setVisible(1);
        testMenu.setStatus(1);
        testMenu.setParent(parentMenu);

        testMenuResponse = new MenuResponse(
                testMenu.getId(),
                testMenu.getParentId(),
                testMenu.getType(),
                testMenu.getName(),
                testMenu.getPath(),
                testMenu.getComponent(),
                testMenu.getPermKey(),
                testMenu.getIcon(),
                testMenu.getSortOrder(),
                testMenu.getVisible(),
                testMenu.getStatus(),
                null,
                testMenu.getCreateTime(),
                testMenu.getUpdateTime()
        );

        // Mock conversionService - use Answer to dynamically create Response
        lenient().when(conversionService.convert(any(MenuEntity.class), eq(MenuResponse.class)))
                .thenAnswer(invocation -> {
                    MenuEntity entity = invocation.getArgument(0);
                    // Use parent's id if parent is set, otherwise use parentId field
                    // For top-level menus (parent is null), return "0" as parentId
                    String actualParentId;
                    if (entity.getParent() != null) {
                        actualParentId = entity.getParent().getId();
                    } else if (entity.getParentId() != null) {
                        actualParentId = entity.getParentId();
                    } else {
                        // Top-level menu with no parent
                        actualParentId = "0";
                    }
                    return new MenuResponse(
                            entity.getId(),
                            actualParentId,
                            entity.getType(),
                            entity.getName(),
                            entity.getPath(),
                            entity.getComponent(),
                            entity.getPermKey(),
                            entity.getIcon(),
                            entity.getSortOrder(),
                            entity.getVisible(),
                            entity.getStatus(),
                            null,
                            entity.getCreateTime(),
                            entity.getUpdateTime()
                    );
                });
        lenient().when(conversionService.convert(any(MenuCreateRequest.class), eq(MenuEntity.class)))
                .thenAnswer(invocation -> {
                    MenuCreateRequest req = invocation.getArgument(0);
                    MenuEntity entity = new MenuEntity();
                    entity.setId(testMenu.getId());
                    entity.setParentId(req.parentId());
                    entity.setType(req.type());
                    entity.setName(req.name());
                    entity.setPath(req.path());
                    entity.setComponent(req.component());
                    entity.setPermKey(req.permKey());
                    entity.setIcon(req.icon());
                    entity.setSortOrder(req.sortOrder());
                    entity.setVisible(req.visible());
                    entity.setStatus(req.status());
                    return entity;
                });
    }

    @Nested
    @DisplayName("getMenuById Tests")
    class GetMenuByIdTests {

        @Test
        @DisplayName("should return menu when exists")
        void getMenuById_WhenExists_ShouldReturnMenu() {
            // Given
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));

            // When
            MenuResponse result = menuService.getMenuById("menu-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Menu");
        }

        @Test
        @DisplayName("should throw exception when menu not found")
        void getMenuById_WhenNotFound_ShouldThrowException() {
            // Given
            when(menuRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.getMenuById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("菜单不存在");
        }
    }

    @Nested
    @DisplayName("getMenuTree Tests")
    class GetMenuTreeTests {

        @Test
        @DisplayName("should return empty list when no menus")
        void getMenuTree_WhenNoMenus_ShouldReturnEmptyList() {
            // Given
            when(menuRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());

            // When
            List<MenuResponse> result = menuService.getMenuTree();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return menu tree")
        void getMenuTree_ShouldReturnMenuTree() {
            // Given
            when(menuRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(parentMenu, testMenu));

            // When
            List<MenuResponse> result = menuService.getMenuTree();

            // Then
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("createMenu Tests")
    class CreateMenuTests {

        @Test
        @DisplayName("should create menu without parent")
        void createMenu_WithoutParent_ShouldCreateMenu() {
            // Given
            MenuCreateRequest req = new MenuCreateRequest(
                    null, 1, "Root Menu", "/root", "RootComponent",
                    "root:view", "icon", 1, 1, 1
            );
            MenuEntity newMenu = new MenuEntity();
            newMenu.setId("new-menu");
            newMenu.setName("Root Menu");
            when(menuRepository.save(any())).thenReturn(newMenu);

            // When
            MenuResponse result = menuService.createMenu(req);

            // Then
            assertThat(result).isNotNull();
            verify(menuRepository).save(any(MenuEntity.class));
        }

        @Test
        @DisplayName("should create menu with parent")
        void createMenu_WithParent_ShouldCreateMenu() {
            // Given
            MenuCreateRequest req = new MenuCreateRequest(
                    "parent-001", 2, "Child Menu", "/child", "ChildComponent",
                    "child:view", "icon", 1, 1, 1
            );
            when(menuRepository.findById("parent-001")).thenReturn(Optional.of(parentMenu));
            when(menuRepository.save(any())).thenReturn(testMenu);

            // When
            MenuResponse result = menuService.createMenu(req);

            // Then
            assertThat(result).isNotNull();
            verify(menuRepository).save(any(MenuEntity.class));
        }

        @Test
        @DisplayName("should throw exception when parent not found")
        void createMenu_WithNonExistentParent_ShouldThrowException() {
            // Given
            MenuCreateRequest req = new MenuCreateRequest(
                    "non-existent", 2, "Child Menu", "/child", "ChildComponent",
                    "child:view", "icon", 1, 1, 1
            );
            when(menuRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("父菜单不存在");
        }
    }

    @Nested
    @DisplayName("deleteMenu Tests")
    class DeleteMenuTests {

        @Test
        @DisplayName("should delete menu without children")
        void deleteMenu_WithoutChildren_ShouldDelete() {
            // Given
            testMenu.setChildren(List.of());
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));

            // When
            menuService.deleteMenu("menu-001");

            // Then
            verify(menuRepository).delete(testMenu);
        }

        @Test
        @DisplayName("should throw exception when menu has children")
        void deleteMenu_WithChildren_ShouldThrowException() {
            // Given
            MenuEntity childMenu = new MenuEntity();
            childMenu.setId("child-001");
            testMenu.setChildren(List.of(childMenu));
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));

            // When & Then
            assertThatThrownBy(() -> menuService.deleteMenu("menu-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("存在子菜单");
        }

        @Test
        @DisplayName("should throw exception when menu not found")
        void deleteMenu_WhenNotFound_ShouldThrowException() {
            // Given
            when(menuRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> menuService.deleteMenu("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("菜单不存在");
        }
    }

    @Nested
    @DisplayName("getUserMenuTree Tests")
    class GetUserMenuTreeTests {

        @Test
        @DisplayName("should return empty list when user has no roles")
        void getUserMenuTree_WhenNoRoles_ShouldReturnEmptyList() {
            // Given
            when(userRoleRepository.findByUserId("user-001")).thenReturn(List.of());

            // When
            List<MenuResponse> result = menuService.getUserMenuTree("user-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("copyMenu Tests")
    class CopyMenuTests {

        @Test
        @DisplayName("should copy menu to top level when targetParentId is 0")
        void copyMenu_WhenTargetIsTopLevel_ShouldCopySuccessfully() {
            // Given
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));
            when(menuRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(parentMenu, testMenu));
            when(menuRepository.save(any(MenuEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            MenuResponse result = menuService.copyMenu("menu-001", "0");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Menu (副本)");
            assertThat(result.parentId()).isEqualTo("0");
            verify(menuRepository).save(any(MenuEntity.class));
        }

        @Test
        @DisplayName("should throw exception when copying to child menu")
        void copyMenu_WhenTargetIsChild_ShouldThrowException() {
            // Given - setup a child relationship
            MenuEntity childMenu = new MenuEntity();
            childMenu.setId("child-001");
            childMenu.setParent(testMenu);
            childMenu.setAncestors("0,parent-001,menu-001,");  // Set ancestors properly

            // Mock findById to return appropriate menu based on ID
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));
            when(menuRepository.findById("child-001")).thenReturn(Optional.of(childMenu));

            // When & Then
            assertThatThrownBy(() -> menuService.copyMenu("menu-001", "child-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不能将菜单复制到自己的子菜单下");
        }

        @Test
        @DisplayName("should copy to specified parent when targetParentId is provided")
        void copyMenu_WhenTargetIsProvided_ShouldCopyToParent() {
            // Given
            when(menuRepository.findById("menu-001")).thenReturn(Optional.of(testMenu));
            when(menuRepository.findById("parent-001")).thenReturn(Optional.of(parentMenu));
            when(menuRepository.save(any(MenuEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            MenuResponse result = menuService.copyMenu("menu-001", "parent-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Menu (副本)");
            assertThat(result.parentId()).isEqualTo("parent-001");
            verify(menuRepository).save(any(MenuEntity.class));
        }
    }
}