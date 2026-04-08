package com.adminplus.controller;

import com.adminplus.pojo.dto.req.MenuCreateReq;
import com.adminplus.pojo.dto.req.MenuUpdateReq;
import com.adminplus.pojo.dto.resp.MenuResp;
import com.adminplus.service.MenuService;
import com.adminplus.config.TestJacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MenuController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuController Unit Tests")
class MenuControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private ObjectMapper objectMapper;
    private MenuResp testMenu;
    private MenuCreateReq createReq;
    private MenuUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        testMenu = new MenuResp(
                "menu-001", null, 1, "系统管理", "/system",
                "system/index", "system:view", "setting", 1, 1, 1,
                List.of(), Instant.now(), Instant.now()
        );
        createReq = new MenuCreateReq(
                null, 1, "系统管理", "/system", "system/index",
                "system:view", "setting", 1, 1, 1
        );
        updateReq = new MenuUpdateReq(
                Optional.empty(), Optional.of(1), Optional.of("系统管理"), Optional.of("/system"), Optional.of("system/index"),
                Optional.of("system:view"), Optional.of("setting"), Optional.of(1), Optional.of(1), Optional.of(1)
        );
    }

    @Nested
    @DisplayName("getMenuTree Tests")
    class GetMenuTreeTests {

        @Test
        @DisplayName("should return menu tree")
        void getMenuTree_ShouldReturnTree() throws Exception {
            // Given
            when(menuService.getMenuTree()).thenReturn(List.of(testMenu));

            // When & Then
            mockMvc.perform(get("/v1/sys/menus/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].name").value("系统管理"));

            verify(menuService).getMenuTree();
        }
    }

    @Nested
    @DisplayName("getMenuById Tests")
    class GetMenuByIdTests {

        @Test
        @DisplayName("should return menu by id")
        void getMenuById_ShouldReturnMenu() throws Exception {
            // Given
            when(menuService.getMenuById("menu-001")).thenReturn(testMenu);

            // When & Then
            mockMvc.perform(get("/v1/sys/menus/menu-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("系统管理"));

            verify(menuService).getMenuById("menu-001");
        }
    }

    @Nested
    @DisplayName("createMenu Tests")
    class CreateMenuTests {

        @Test
        @DisplayName("should create menu")
        void createMenu_ShouldCreateMenu() throws Exception {
            // Given
            when(menuService.createMenu(any(MenuCreateReq.class))).thenReturn(testMenu);

            // When & Then
            mockMvc.perform(post("/v1/sys/menus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(menuService).createMenu(any(MenuCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateMenu Tests")
    class UpdateMenuTests {

        @Test
        @DisplayName("should update menu")
        void updateMenu_ShouldUpdateMenu() throws Exception {
            // Given
            when(menuService.updateMenu(anyString(), any(MenuUpdateReq.class))).thenReturn(testMenu);

            // When & Then
            mockMvc.perform(put("/v1/sys/menus/menu-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(menuService).updateMenu(anyString(), any(MenuUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteMenu Tests")
    class DeleteMenuTests {

        @Test
        @DisplayName("should delete menu")
        void deleteMenu_ShouldDeleteMenu() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/menus/menu-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(menuService).deleteMenu("menu-001");
        }
    }
}