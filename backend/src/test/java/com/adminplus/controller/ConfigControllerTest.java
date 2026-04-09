package com.adminplus.controller;

import com.adminplus.pojo.dto.query.ConfigQuery;
import com.adminplus.pojo.dto.request.*;
import com.adminplus.pojo.dto.response.*;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.service.ConfigService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 配置控制器测试
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("配置控制器测试")
class ConfigControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigController configController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(configController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
    }

    private ConfigResponse createTestConfigResp() {
        return new ConfigResponse(
                "1",
                "group1",
                "系统配置",
                "系统名称",
                "system.name",
                "AdminPlus",
                "STRING",
                "IMMEDIATE",
                "Default System",
                "系统显示名称",
                false,
                null,
                1,
                1,
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    @DisplayName("查询配置列表")
    class GetConfigListTests {

        @Test
        @DisplayName("成功查询配置列表")
        void shouldReturnConfigList() throws Exception {
            // Given
            PageResultResponse<ConfigResponse> pageResult = new PageResultResponse<>(
                    Collections.singletonList(createTestConfigResp()),
                    1L,
                    1,
                    10
            );
            when(configService.getConfigList(any(ConfigQuery.class))).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].name").value("系统名称"));

            verify(configService).getConfigList(any(ConfigQuery.class));
        }

        @Test
        @DisplayName("成功按配置组和关键字搜索")
        void shouldSearchConfigsByGroupAndKeyword() throws Exception {
            // Given
            PageResultResponse<ConfigResponse> pageResult = new PageResultResponse<>(
                    Collections.singletonList(createTestConfigResp()),
                    1L,
                    1,
                    10
            );
            when(configService.getConfigList(any(ConfigQuery.class))).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs")
                            .param("page", "1")
                            .param("size", "10")
                            .param("groupId", "group1")
                            .param("keyword", "system")
                            .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].name").value("系统名称"));

            verify(configService).getConfigList(any(ConfigQuery.class));
        }
    }

    @Nested
    @DisplayName("根据配置组查询配置")
    class GetConfigsByGroupIdTests {

        @Test
        @DisplayName("成功根据配置组ID查询配置列表")
        void shouldReturnConfigsByGroupId() throws Exception {
            // Given
            List<ConfigResponse> configs = Collections.singletonList(createTestConfigResp());
            when(configService.getConfigsByGroupId("group1")).thenReturn(configs);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs/group/group1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("系统名称"));

            verify(configService).getConfigsByGroupId("group1");
        }
    }

    @Nested
    @DisplayName("根据配置组编码查询配置")
    class GetConfigsByGroupCodeTests {

        @Test
        @DisplayName("成功根据配置组编码查询配置列表")
        void shouldReturnConfigsByGroupCode() throws Exception {
            // Given
            List<ConfigResponse> configs = Collections.singletonList(createTestConfigResp());
            when(configService.getConfigsByGroupCode("basic")).thenReturn(configs);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs/group-code/basic"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("系统名称"));

            verify(configService).getConfigsByGroupCode("basic");
        }
    }

    @Nested
    @DisplayName("根据配置键查询")
    class GetConfigByKeyTests {

        @Test
        @DisplayName("成功根据配置键查询")
        void shouldReturnConfigByKey() throws Exception {
            // Given
            ConfigResponse config = createTestConfigResp();
            when(configService.getConfigByKey("system.name")).thenReturn(config);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs/key/system.name"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.key").value("system.name"))
                    .andExpect(jsonPath("$.data.name").value("系统名称"));

            verify(configService).getConfigByKey("system.name");
        }
    }

    @Nested
    @DisplayName("根据ID查询配置")
    class GetConfigByIdTests {

        @Test
        @DisplayName("成功根据ID查询配置")
        void shouldReturnConfigById() throws Exception {
            // Given
            ConfigResponse config = createTestConfigResp();
            when(configService.getConfigById("1")).thenReturn(config);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.name").value("系统名称"));

            verify(configService).getConfigById("1");
        }
    }

    @Nested
    @DisplayName("创建配置")
    class CreateConfigTests {

        @Test
        @DisplayName("成功创建配置")
        void shouldCreateConfig() throws Exception {
            // Given
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group1",
                    "数据库URL",
                    "database.url",
                    "jdbc:mysql://localhost:3306/adminplus",
                    "STRING",
                    "RESTART",
                    "jdbc:mysql://localhost:3306/mydb",
                    "数据库连接URL",
                    true,
                    null,
                    2
            );
            ConfigResponse result = createTestConfigResp();
            when(configService.createConfig(any(ConfigCreateRequest.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/v1/sys/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("系统名称"));

            verify(configService).createConfig(any(ConfigCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("更新配置")
    class UpdateConfigTests {

        @Test
        @DisplayName("成功更新配置")
        void shouldUpdateConfig() throws Exception {
            // Given
            ConfigUpdateRequest req = new ConfigUpdateRequest(
                    "系统名称（已更新）",
                    "AdminPlus V2",
                    null,
                    null,
                    null,
                    "更新后的描述",
                    null,
                    null,
                    null,
                    null
            );
            ConfigResponse result = createTestConfigResp();
            when(configService.updateConfig(eq("1"), any(ConfigUpdateRequest.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(put("/v1/sys/configs/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("系统名称"));

            verify(configService).updateConfig(eq("1"), any(ConfigUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("删除配置")
    class DeleteConfigTests {

        @Test
        @DisplayName("成功删除配置")
        void shouldDeleteConfig() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/configs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).deleteConfig("1");
        }
    }

    @Nested
    @DisplayName("更新配置状态")
    class UpdateConfigStatusTests {

        @Test
        @DisplayName("成功更新配置状态")
        void shouldUpdateConfigStatus() throws Exception {
            // When & Then
            mockMvc.perform(put("/v1/sys/configs/1/status")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).updateConfigStatus("1", 0);
        }
    }

    @Nested
    @DisplayName("批量更新配置")
    class BatchUpdateConfigsTests {

        @Test
        @DisplayName("成功批量更新配置值")
        void shouldBatchUpdateConfigs() throws Exception {
            // Given
            ConfigBatchUpdateRequest req = new ConfigBatchUpdateRequest(
                    Collections.singletonList(
                            new ConfigBatchUpdateRequest.ConfigItemUpdate("1", "new value")
                    )
            );
            ConfigImportResultResponse result = new ConfigImportResultResponse(
                    1, 1, 0, 0,
                    Collections.singletonList(
                            new ConfigImportResultResponse.ImportDetail("system.name", "success", null)
                    )
            );
            when(configService.batchUpdateConfigs(any(ConfigBatchUpdateRequest.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/v1/sys/configs/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.success").value(1));

            verify(configService).batchUpdateConfigs(any(ConfigBatchUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("导出配置")
    class ExportConfigsTests {

        @Test
        @DisplayName("成功导出配置")
        void shouldExportConfigs() throws Exception {
            // Given
            ConfigExportResponse result = new ConfigExportResponse(
                    "1.0",
                    "2026-03-30 12:00:00",
                    Collections.singletonList(
                            new ConfigExportResponse.ExportGroup(
                                    "system",
                                    "系统配置",
                                    "Settings",
                                    Collections.singletonList(
                                            new ConfigExportResponse.ExportConfig(
                                                    "system.name",
                                                    "系统名称",
                                                    "AdminPlus",
                                                    "STRING",
                                                    "IMMEDIATE",
                                                    "系统显示名称"
                                            )
                                    )
                            )
                    )
            );
            when(configService.exportConfigs(null)).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/v1/sys/configs/export"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.exportVersion").value("1.0"));

            verify(configService).exportConfigs(null);
        }
    }

    @Nested
    @DisplayName("导入配置")
    class ImportConfigsTests {

        @Test
        @DisplayName("成功导入配置")
        void shouldImportConfigs() throws Exception {
            // Given
            ConfigImportRequest req = new ConfigImportRequest(
                    "{\"items\":[{\"key\":\"system.name\",\"value\":\"AdminPlus\"}]}",
                    "JSON",
                    "OVERWRITE"
            );
            ConfigImportResultResponse result = new ConfigImportResultResponse(
                    1, 1, 0, 0,
                    Collections.singletonList(
                            new ConfigImportResultResponse.ImportDetail("system.name", "success", null)
                    )
            );
            when(configService.importConfigs(any(ConfigImportRequest.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/v1/sys/configs/import")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.success").value(1));

            verify(configService).importConfigs(any(ConfigImportRequest.class));
        }
    }

    @Nested
    @DisplayName("回滚配置")
    class RollbackConfigTests {

        @Test
        @DisplayName("成功回滚配置")
        void shouldRollbackConfig() throws Exception {
            // Given
            ConfigRollbackRequest req = new ConfigRollbackRequest("history123", "回滚测试");
            ConfigResponse result = createTestConfigResp();
            when(configService.rollbackConfig(eq("1"), any(ConfigRollbackRequest.class))).thenReturn(result);

            // When & Then
            mockMvc.perform(post("/v1/sys/configs/1/rollback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("系统名称"));

            verify(configService).rollbackConfig(eq("1"), any(ConfigRollbackRequest.class));
        }
    }

    @Nested
    @DisplayName("查询配置历史")
    class GetConfigHistoryTests {

        @Test
        @DisplayName("成功查询配置历史记录")
        void shouldReturnConfigHistory() throws Exception {
            // Given
            List<ConfigHistoryResponse> history = Collections.singletonList(
                    new ConfigHistoryResponse(
                            "h1",
                            "1",
                            "system.name",
                            "Old Value",
                            "New Value",
                            "操作: 更新值",
                            null,
                            Instant.now()
                    )
            );
            when(configService.getConfigHistory("1")).thenReturn(history);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs/1/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].configKey").value("system.name"));

            verify(configService).getConfigHistory("1");
        }
    }

    @Nested
    @DisplayName("获取配置生效信息")
    class GetConfigEffectInfoTests {

        @Test
        @DisplayName("成功获取配置生效信息")
        void shouldReturnConfigEffectInfo() throws Exception {
            // Given
            ConfigEffectInfoResponse result = new ConfigEffectInfoResponse(
                    Collections.singletonList(
                            new ConfigEffectInfoResponse.PendingEffect(
                                    "system.name",
                                    "系统名称",
                                    "AdminPlus",
                                    "MANUAL",
                                    "2026-03-30 12:00:00"
                            )
                    ),
                    Collections.singletonList("database.url")
            );
            when(configService.getConfigEffectInfo()).thenReturn(result);

            // When & Then
            mockMvc.perform(get("/v1/sys/configs/effect-info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pendingEffects[0].key").value("system.name"))
                    .andExpect(jsonPath("$.data.restartRequiredConfigs[0]").value("database.url"));

            verify(configService).getConfigEffectInfo();
        }
    }

    @Nested
    @DisplayName("手动生效配置")
    class ApplyConfigTests {

        @Test
        @DisplayName("成功手动生效配置")
        void shouldApplyConfig() throws Exception {
            // When & Then
            mockMvc.perform(post("/v1/sys/configs/1/apply"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(configService).applyConfig("1");
        }
    }
}
