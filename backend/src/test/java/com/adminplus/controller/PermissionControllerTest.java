package com.adminplus.controller;

import com.adminplus.pojo.dto.response.PermissionResponse;
import com.adminplus.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PermissionController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionController Unit Tests")
class PermissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionController permissionController;

    private PermissionResponse testPermission;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(permissionController).build();
        testPermission = new PermissionResponse(
                "perm-001", "user:view", "查看用户", 1, null
        );
    }

    @Nested
    @DisplayName("getAllPermissions Tests")
    class GetAllPermissionsTests {

        @Test
        @DisplayName("should return all permissions")
        void getAllPermissions_ShouldReturnPermissions() throws Exception {
            // Given
            when(permissionService.getAllPermissions()).thenReturn(List.of(testPermission));

            // When & Then
            mockMvc.perform(get("/v1/sys/permissions/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].permKey").value("user:view"));

            verify(permissionService).getAllPermissions();
        }
    }
}