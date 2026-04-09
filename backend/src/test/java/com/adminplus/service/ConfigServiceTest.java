package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.query.ConfigQuery;
import com.adminplus.pojo.dto.request.*;
import com.adminplus.pojo.dto.request.LogEntry;
import com.adminplus.pojo.dto.response.*;
import com.adminplus.pojo.entity.ConfigEntity;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.pojo.entity.ConfigHistoryEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigHistoryRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.impl.ConfigServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ConfigService 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigService Unit Tests")
class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private ConfigGroupRepository configGroupRepository;

    @Mock
    private ConfigHistoryRepository configHistoryRepository;

    @Mock
    private LogService logService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ConfigServiceImpl configService;

    private ConfigEntity testConfig;
    private ConfigGroupEntity testGroup;
    private ConfigResponse testConfigResponse;

    @BeforeEach
    void setUp() {
        testGroup = new ConfigGroupEntity();
        testGroup.setId("group-001");
        testGroup.setCode("system");
        testGroup.setName("系统配置");
        testGroup.setSortOrder(1);
        testGroup.setStatus(1);

        testConfig = new ConfigEntity();
        testConfig.setId("config-001");
        testConfig.setGroupId("group-001");
        testConfig.setName("测试配置");
        testConfig.setKey("test.key");
        testConfig.setValue("test-value");
        testConfig.setValueType("STRING");
        testConfig.setEffectType("IMMEDIATE");
        testConfig.setDescription("测试配置项");
        testConfig.setSortOrder(1);
        testConfig.setStatus(1);

        testConfigResponse = new ConfigResponse(
                testConfig.getId(),
                testConfig.getGroupId(),
                testGroup.getName(),
                testConfig.getName(),
                testConfig.getKey(),
                testConfig.getValue(),
                testConfig.getValueType(),
                testConfig.getEffectType(),
                testConfig.getDefaultValue(),
                testConfig.getDescription(),
                testConfig.getIsRequired(),
                testConfig.getValidationRule(),
                testConfig.getSortOrder(),
                testConfig.getStatus(),
                testConfig.getCreateTime(),
                testConfig.getUpdateTime()
        );

        // Mock conversionService
        lenient().when(conversionService.convert(any(ConfigEntity.class), eq(ConfigResponse.class)))
                .thenReturn(testConfigResponse);
        lenient().when(conversionService.convert(any(ConfigCreateRequest.class), eq(ConfigEntity.class)))
                .thenReturn(testConfig);
    }

    @Nested
    @DisplayName("getConfigById Tests")
    class GetConfigByIdTests {

        @Test
        @DisplayName("should return config when exists")
        void getConfigById_WhenExists_ShouldReturnConfig() {
            // Given
            when(configRepository.findById("config-001")).thenReturn(Optional.of(testConfig));

            // When
            ConfigResponse result = configService.getConfigById("config-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.key()).isEqualTo("test.key");
        }

        @Test
        @DisplayName("should throw exception when config not found")
        void getConfigById_WhenNotFound_ShouldThrowException() {
            // Given
            when(configRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.getConfigById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置不存在");
        }
    }

    @Nested
    @DisplayName("getConfigByKey Tests")
    class GetConfigByKeyTests {

        @Test
        @DisplayName("should return config when key exists")
        void getConfigByKey_WhenExists_ShouldReturnConfig() {
            // Given
            when(configRepository.findByKey("test.key")).thenReturn(Optional.of(testConfig));

            // When
            ConfigResponse result = configService.getConfigByKey("test.key");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.key()).isEqualTo("test.key");
        }

        @Test
        @DisplayName("should throw exception when key not found")
        void getConfigByKey_WhenNotFound_ShouldThrowException() {
            // Given
            when(configRepository.findByKey("non-existent-key")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.getConfigByKey("non-existent-key"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置不存在");
        }
    }

    @Nested
    @DisplayName("getConfigsByGroupId Tests")
    class GetConfigsByGroupIdTests {

        @Test
        @DisplayName("should return configs when group exists")
        void getConfigsByGroupId_WhenGroupExists_ShouldReturnConfigs() {
            // Given
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.findByGroupIdOrderBySortOrderAsc("group-001"))
                    .thenReturn(List.of(testConfig));
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));

            // When
            List<ConfigResponse> result = configService.getConfigsByGroupId("group-001");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).groupId()).isEqualTo("group-001");
        }

        @Test
        @DisplayName("should throw exception when group not found")
        void getConfigsByGroupId_WhenGroupNotFound_ShouldThrowException() {
            // Given
            when(configGroupRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.getConfigsByGroupId("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置组不存在");
        }
    }

    @Nested
    @DisplayName("createConfig Tests")
    class CreateConfigTests {

        @Test
        @DisplayName("should create config successfully")
        void createConfig_ShouldCreateConfig() {
            // Given
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group-001", "新配置", "new.key", "new-value",
                    "STRING", "IMMEDIATE", null, "新配置项",
                    false, null, 1
            );
            when(configRepository.existsByKey("new.key")).thenReturn(false);
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.save(any())).thenReturn(testConfig);
            when(configHistoryRepository.save(any())).thenReturn(new ConfigHistoryEntity());

            // When
            ConfigResponse result = configService.createConfig(req);

            // Then
            assertThat(result).isNotNull();
            verify(configRepository).save(any(ConfigEntity.class));
            verify(configHistoryRepository).save(any(ConfigHistoryEntity.class));
            verify(logService).log(any(LogEntry.class));
        }

        @Test
        @DisplayName("should throw exception when key already exists")
        void createConfig_WhenKeyExists_ShouldThrowException() {
            // Given
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group-001", "新配置", "existing.key", "new-value",
                    "STRING", "IMMEDIATE", null, "新配置项",
                    false, null, 1
            );
            when(configRepository.existsByKey("existing.key")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> configService.createConfig(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置键已存在");
        }
    }

    @Nested
    @DisplayName("updateConfig Tests")
    class UpdateConfigTests {

        @Test
        @DisplayName("should update config successfully")
        void updateConfig_ShouldUpdateConfig() {
            // Given
            ConfigUpdateRequest req = new ConfigUpdateRequest(
                    "更新配置", "updated-value", null, null, null,
                    "更新描述", null, null, null, 1
            );
            when(configRepository.findById("config-001")).thenReturn(Optional.of(testConfig));
            when(configRepository.save(any())).thenReturn(testConfig);
            when(configHistoryRepository.save(any())).thenReturn(new ConfigHistoryEntity());

            // When
            ConfigResponse result = configService.updateConfig("config-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(configRepository).save(any(ConfigEntity.class));
        }

        @Test
        @DisplayName("should throw exception when config not found")
        void updateConfig_WhenNotFound_ShouldThrowException() {
            // Given
            ConfigUpdateRequest req = new ConfigUpdateRequest(
                    null, "value", null, null, null, null, null, null, null, null
            );
            when(configRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.updateConfig("non-existent", req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置不存在");
        }
    }

    @Nested
    @DisplayName("deleteConfig Tests")
    class DeleteConfigTests {

        @Test
        @DisplayName("should delete config successfully")
        void deleteConfig_ShouldDeleteConfig() {
            // Given
            when(configRepository.findById("config-001")).thenReturn(Optional.of(testConfig));
            doNothing().when(configRepository).deleteById("config-001");

            // When
            configService.deleteConfig("config-001");

            // Then
            verify(configRepository).deleteById("config-001");
            verify(logService).log(any(LogEntry.class));
        }

        @Test
        @DisplayName("should throw exception when config not found")
        void deleteConfig_WhenNotFound_ShouldThrowException() {
            // Given
            when(configRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.deleteConfig("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置不存在");
        }
    }

    @Nested
    @DisplayName("updateConfigStatus Tests")
    class UpdateConfigStatusTests {

        @Test
        @DisplayName("should update status successfully")
        void updateConfigStatus_ShouldUpdateStatus() {
            // Given
            when(configRepository.findById("config-001")).thenReturn(Optional.of(testConfig));
            when(configRepository.save(any())).thenReturn(testConfig);

            // When
            configService.updateConfigStatus("config-001", 0);

            // Then
            verify(configRepository).save(any(ConfigEntity.class));
        }

        @Test
        @DisplayName("should throw exception when config not found")
        void updateConfigStatus_WhenNotFound_ShouldThrowException() {
            // Given
            when(configRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.updateConfigStatus("non-existent", 0))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("配置不存在");
        }
    }

    @Nested
    @DisplayName("getConfigList Tests")
    class GetConfigListTests {

        @Test
        @DisplayName("should return paginated config list")
        void getConfigList_ShouldReturnPaginatedList() {
            // Given
            when(configRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testConfig)));
            when(configGroupRepository.findAllById(any(List.class))).thenReturn(List.of(testGroup));

            // When - use record constructor
            ConfigQuery query = new ConfigQuery(1, 20, null, null, null);
            PageResultResponse<ConfigResponse> result = configService.getConfigList(query);

            // Then
            assertThat(result.records()).hasSize(1);
            assertThat(result.total()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("validateConfigValue Tests")
    class ValidateConfigValueTests {

        @Test
        @DisplayName("should validate NUMBER type successfully")
        void validateConfigValue_NumberType_ShouldPass() {
            // Given - no need for objectMapper mock since NUMBER validation doesn't use it

            // When - creating a NUMBER config with valid value
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group-001", "数字配置", "num.key", "123.45",
                    "NUMBER", "IMMEDIATE", null, "数字配置项",
                    false, null, 1
            );
            when(configRepository.existsByKey("num.key")).thenReturn(false);
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.save(any())).thenReturn(testConfig);

            // Then - should not throw exception
            configService.createConfig(req);
            verify(configRepository).save(any(ConfigEntity.class));
        }

        @Test
        @DisplayName("should throw exception for invalid NUMBER type")
        void validateConfigValue_InvalidNumber_ShouldThrowException() {
            // Given
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group-001", "数字配置", "num.key", "not-a-number",
                    "NUMBER", "IMMEDIATE", null, "数字配置项",
                    false, null, 1
            );
            when(configRepository.existsByKey("num.key")).thenReturn(false);
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));

            // When & Then
            assertThatThrownBy(() -> configService.createConfig(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("必须是有效的数字");
        }

        @Test
        @DisplayName("should validate BOOLEAN type successfully")
        void validateConfigValue_BooleanType_ShouldPass() {
            // Given
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group-001", "布尔配置", "bool.key", "true",
                    "BOOLEAN", "IMMEDIATE", null, "布尔配置项",
                    false, null, 1
            );
            when(configRepository.existsByKey("bool.key")).thenReturn(false);
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));
            when(configRepository.save(any())).thenReturn(testConfig);

            // Then - should not throw exception
            configService.createConfig(req);
            verify(configRepository).save(any(ConfigEntity.class));
        }

        @Test
        @DisplayName("should throw exception for invalid BOOLEAN type")
        void validateConfigValue_InvalidBoolean_ShouldThrowException() {
            // Given
            ConfigCreateRequest req = new ConfigCreateRequest(
                    "group-001", "布尔配置", "bool.key", "maybe",
                    "BOOLEAN", "IMMEDIATE", null, "布尔配置项",
                    false, null, 1
            );
            when(configRepository.existsByKey("bool.key")).thenReturn(false);
            when(configGroupRepository.findById("group-001")).thenReturn(Optional.of(testGroup));

            // When & Then
            assertThatThrownBy(() -> configService.createConfig(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("必须是 true 或 false");
        }
    }
}