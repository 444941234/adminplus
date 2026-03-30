package com.adminplus.controller;

import com.adminplus.common.annotation.OperationLog;
import com.adminplus.pojo.dto.req.ConfigGroupCreateReq;
import com.adminplus.pojo.dto.req.ConfigGroupUpdateReq;
import com.adminplus.pojo.dto.resp.ConfigGroupResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.ConfigGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 配置组控制器测试
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@WebMvcTest(ConfigGroupController.class)
@ActiveProfiles("test")
@DisplayName("配置组控制器测试")
class ConfigGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigGroupService configGroupService;

    private ConfigGroupResp createTestGroupResp() {
        return new ConfigGroupResp(
                "1",
                "系统配置",
                "system",
                "Settings",
                1,
                "系统相关配置",
                1,
                0L,
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    @DisplayName("查询配置组列表")
    class GetConfigGroupListTests {

        @Test
        @WithMockUser(authorities = "config:group:list")
        @DisplayName("成功查询配置组列表")
        void shouldReturnConfigGroupList() throws Exception {
            // Given
            PageResultResp<ConfigGroupResp> pageResult = new PageResultResp<>(
                    Collections.singletonList(createTestGroupResp()),
                    1L,
                    1,
                    10
            );
            when(configGroupService.getConfigGroupList(1, 10, null)).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/config-groups")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].name").value("系统配置"));

            verify(configGroupService).getConfigGroupList(1, 10, null);
        }

        @Test
        @WithMockUser(authorities = "config:group:list")
        @DisplayName("成功按关键字搜索配置组")
        void shouldSearchConfigGroupsByKeyword() throws Exception {
            // Given
            PageResultResp<ConfigGroupResp> pageResult = new PageResultResp<>(
                    Collections.singletonList(createTestGroupResp()),
                    1L,
                    1,
                    10
            );
            when(configGroupService.getConfigGroupList(1, 10, "系统")).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/config-groups")
                            .param("page", "1")
                            .param("size", "10")
                            .param("keyword", "系统"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].name").value("系统配置"));

            verify(configGroupService).getConfigGroupList(1, 10, "系统");
        }
    }

    @Nested
    @DisplayName("查询启用的配置组")
    class GetActiveConfigGroupsTests {

        @Test
        @WithMockUser(authorities = "config:group:query")
        @DisplayName("成功查询启用的配置组列表")
        void shouldReturnActiveConfigGroups() throws Exception {
            // Given
            List<ConfigGroupResp> groups = Collections.singletonList(createTestGroupResp());
            when(configGroupService.getActiveConfigGroups()).thenReturn(groups);

            // When & Then
            mockMvc.perform(get("/v1/sys/config-groups/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("系统配置"))
                    .andExpect(jsonPath("$.data[0].status").value(1));

            verify(configGroupService).getActiveConfigGroups();
        }
    }

    @Nested
    @DisplayName("根据编码查询配置组")
    class GetConfigGroupByCodeTests {

        @Test
        @WithMockUser(authorities = "config:group:query")
        @DisplayName("成功根据编码查询配置组")
        void shouldReturnConfigGroupByCode() throws Exception {
            // Given
            ConfigGroupResp group = createTestGroupResp();
            when(configGroupService.getConfigGroupByCode("system")).thenReturn(group);

            // When & Then
            mockMvc.perform(get("/v1/sys/config-groups/code/system"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value("system"))
                    .andExpect(jsonPath("$.data.name").value("系统配置"));

            verify(configGroupService).getConfigGroupByCode("system");
        }
    }

    @Nested
    @DisplayName("根据ID查询配置组")
    class GetConfigGroupByIdTests {

        @Test
        @WithMockUser(authorities = "config:group:query")
        @DisplayName("成功根据ID查询配置组")
        void shouldReturnConfigGroupById() throws Exception {
            // Given
            ConfigGroupResp group = createTestGroupResp();
            when(configGroupService.getConfigGroupById("1")).thenReturn(group);

            // When & Then
            mockMvc.perform(get("/v1/sys/config-groups/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.name").value("系统配置"));

            verify(configGroupService).getConfigGroupById("1");
        }
    }

    @Nested
    @DisplayName("创建配置组")
    class CreateConfigGroupTests {

        @Test
        @WithMockUser(authorities = "config:group:add")
        @DisplayName("成功创建配置组")
        void shouldCreateConfigGroup() throws Exception {
            // Given
            ConfigGroupCreateReq req = new ConfigGroupCreateReq(
                    "数据库配置",
                    "database",
                    "Database",
                    2,
                    "数据库相关配置"
            );
            ConfigGroupResp result = new ConfigGroupResp(
                    "2",
                    "数据库配置",
                    "database",
                    "Database",
                    2,
                    "数据库相关配置",
                    1,
                    0L,
                    Instant.now(),
                    Instant.now()
            );
            when(configGroupService.createConfigGroup(any(ConfigGroupCreateReq.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/v1/sys/config-groups")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("数据库配置"))
                    .andExpect(jsonPath("$.data.code").value("database"));

            verify(configGroupService).createConfigGroup(any(ConfigGroupCreateReq.class));
        }
    }

    @Nested
    @DisplayName("更新配置组")
    class UpdateConfigGroupTests {

        @Test
        @WithMockUser(authorities = "config:group:edit")
        @DisplayName("成功更新配置组")
        void shouldUpdateConfigGroup() throws Exception {
            // Given
            ConfigGroupUpdateReq req = new ConfigGroupUpdateReq(
                    "系统配置（已更新）",
                    "Settings2",
                    2,
                    "更新后的描述"
            );
            ConfigGroupResp result = createTestGroupResp();
            when(configGroupService.updateConfigGroup(eq("1"), any(ConfigGroupUpdateReq.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(put("/v1/sys/config-groups/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("系统配置"));

            verify(configGroupService).updateConfigGroup(eq("1"), any(ConfigGroupUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("删除配置组")
    class DeleteConfigGroupTests {

        @Test
        @WithMockUser(authorities = "config:group:delete")
        @DisplayName("成功删除配置组")
        void shouldDeleteConfigGroup() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/config-groups/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configGroupService).deleteConfigGroup("1");
        }
    }

    @Nested
    @DisplayName("更新配置组状态")
    class UpdateConfigGroupStatusTests {

        @Test
        @WithMockUser(authorities = "config:group:edit")
        @DisplayName("成功更新配置组状态")
        void shouldUpdateConfigGroupStatus() throws Exception {
            // When & Then
            mockMvc.perform(put("/v1/sys/config-groups/1/status")
                            .with(csrf())
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configGroupService).updateConfigGroupStatus("1", 0);
        }
    }
}
