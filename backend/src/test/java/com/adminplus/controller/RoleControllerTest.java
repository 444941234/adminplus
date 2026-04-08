package com.adminplus.controller;

import com.adminplus.pojo.dto.query.RoleQuery;
import com.adminplus.pojo.dto.req.RoleCreateReq;
import com.adminplus.pojo.dto.req.RoleUpdateReq;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.dto.resp.RoleResp;
import com.adminplus.service.RoleService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoleController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleController Unit Tests")
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private ObjectMapper objectMapper;
    private RoleResp testRole;
    private RoleCreateReq createReq;
    private RoleUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        objectMapper = new ObjectMapper();
        testRole = new RoleResp(
                "role-001", "ADMIN", "管理员", "系统管理员角色",
                1, 1, 1, Instant.now(), Instant.now()
        );
        createReq = new RoleCreateReq(
                "ADMIN", "管理员", "系统管理员角色", 1, 1, 1
        );
        updateReq = new RoleUpdateReq(
                "管理员", "系统管理员角色", 1, 1, 1
        );
    }

    @Nested
    @DisplayName("getRoleList Tests")
    class GetRoleListTests {

        @Test
        @DisplayName("should return role list with pagination")
        void getRoleList_ShouldReturnRoleList() throws Exception {
            // Given
            PageResultResp<RoleResp> pageResult = new PageResultResp<>(List.of(testRole), 1L, 1, 10);
            when(roleService.getRoleList(any(RoleQuery.class))).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/roles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].name").value("管理员"));

            verify(roleService).getRoleList(any(RoleQuery.class));
        }
    }

    @Nested
    @DisplayName("getRoleById Tests")
    class GetRoleByIdTests {

        @Test
        @DisplayName("should return role by id")
        void getRoleById_ShouldReturnRole() throws Exception {
            // Given
            when(roleService.getRoleById("role-001")).thenReturn(testRole);

            // When & Then
            mockMvc.perform(get("/v1/sys/roles/role-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("管理员"));

            verify(roleService).getRoleById("role-001");
        }
    }

    @Nested
    @DisplayName("createRole Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("should create role")
        void createRole_ShouldCreateRole() throws Exception {
            // Given
            when(roleService.createRole(any(RoleCreateReq.class))).thenReturn(testRole);

            // When & Then
            mockMvc.perform(post("/v1/sys/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(roleService).createRole(any(RoleCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateRole Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("should update role")
        void updateRole_ShouldUpdateRole() throws Exception {
            // Given
            when(roleService.updateRole(anyString(), any(RoleUpdateReq.class))).thenReturn(testRole);

            // When & Then
            mockMvc.perform(put("/v1/sys/roles/role-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(roleService).updateRole(anyString(), any(RoleUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteRole Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("should delete role")
        void deleteRole_ShouldDeleteRole() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/roles/role-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(roleService).deleteRole("role-001");
        }
    }

    @Nested
    @DisplayName("assignMenus Tests")
    class AssignMenusTests {

        @Test
        @DisplayName("should assign menus to role")
        void assignMenus_ShouldAssignMenus() throws Exception {
            // When & Then
            mockMvc.perform(put("/v1/sys/roles/role-001/menus")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"menu-001\", \"menu-002\"]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(roleService).assignMenus("role-001", List.of("menu-001", "menu-002"));
        }
    }

    @Nested
    @DisplayName("getRoleMenuIds Tests")
    class GetRoleMenuIdsTests {

        @Test
        @DisplayName("should return role menu ids")
        void getRoleMenuIds_ShouldReturnMenuIds() throws Exception {
            // Given
            when(roleService.getRoleMenuIds("role-001")).thenReturn(List.of("menu-001", "menu-002"));

            // When & Then
            mockMvc.perform(get("/v1/sys/roles/role-001/menus"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0]").value("menu-001"));

            verify(roleService).getRoleMenuIds("role-001");
        }
    }
}