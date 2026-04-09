package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.query.ConfigGroupQuery;
import com.adminplus.pojo.dto.request.ConfigGroupCreateRequest;
import com.adminplus.pojo.dto.request.ConfigGroupUpdateRequest;
import com.adminplus.pojo.dto.request.LogEntry;
import com.adminplus.pojo.dto.response.ConfigGroupResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.service.impl.ConfigGroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 配置组服务测试
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("配置组服务测试")
class ConfigGroupServiceTest {

    @Mock(lenient = true)
    private ConfigGroupRepository configGroupRepository;

    @Mock(lenient = true)
    private ConfigRepository configRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private ConfigGroupServiceImpl configGroupService;

    private ConfigGroupEntity testGroup;
    private ConfigGroupResponse testGroupResp;

    @BeforeEach
    void setUp() {
        testGroup = new ConfigGroupEntity();
        testGroup.setId("1");
        testGroup.setName("系统配置");
        testGroup.setCode("system");
        testGroup.setIcon("Settings");
        testGroup.setSortOrder(1);
        testGroup.setDescription("系统相关配置");
        testGroup.setStatus(1);
        testGroup.setCreateTime(Instant.now());
        testGroup.setUpdateTime(Instant.now());

        testGroupResp = new ConfigGroupResponse(
                testGroup.getId(),
                testGroup.getName(),
                testGroup.getCode(),
                testGroup.getIcon(),
                testGroup.getSortOrder(),
                testGroup.getDescription(),
                testGroup.getStatus(),
                0L,
                testGroup.getCreateTime(),
                testGroup.getUpdateTime()
        );
    }

    @Nested
    @DisplayName("查询配置组列表")
    class GetConfigGroupListTests {

        @Test
        @DisplayName("成功查询配置组列表")
        void shouldReturnConfigGroupList() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<ConfigGroupEntity> page = new PageImpl<>(List.of(testGroup));
            when(configRepository.countByGroupId(any())).thenReturn(0L);
            doReturn(page).when(configGroupRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

            // When
            ConfigGroupQuery query = new ConfigGroupQuery(1, 10, null);
            PageResultResponse<ConfigGroupResponse> result = configGroupService.getConfigGroupList(query);

            // Then
            assertThat(result.records()).hasSize(1);
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.records().get(0).name()).isEqualTo("系统配置");
        }

        @Test
        @DisplayName("成功按关键字搜索配置组")
        void shouldSearchConfigGroupsByKeyword() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<ConfigGroupEntity> page = new PageImpl<>(List.of(testGroup));
            when(configRepository.countByGroupId(any())).thenReturn(0L);
            doReturn(page).when(configGroupRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

            // When
            ConfigGroupQuery query = new ConfigGroupQuery(1, 10, "系统");
            PageResultResponse<ConfigGroupResponse> result = configGroupService.getConfigGroupList(query);

            // Then
            assertThat(result.records()).hasSize(1);
            verify(configGroupRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("根据ID查询配置组")
    class GetConfigGroupByIdTests {

        @Test
        @DisplayName("成功根据ID查询配置组")
        void shouldReturnConfigGroupById() {
            // Given
            when(configGroupRepository.findById("1")).thenReturn(Optional.of(testGroup));
            when(configRepository.countByGroupId("1")).thenReturn(0L);

            // When
            ConfigGroupResponse result = configGroupService.getConfigGroupById("1");

            // Then
            assertThat(result.id()).isEqualTo("1");
            assertThat(result.name()).isEqualTo("系统配置");
            assertThat(result.code()).isEqualTo("system");
        }

        @Test
        @DisplayName("查询不存在的配置组抛出异常")
        void shouldThrowExceptionWhenConfigGroupNotFound() {
            // Given
            when(configGroupRepository.findById("999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configGroupService.getConfigGroupById("999"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("配置组不存在");
        }
    }

    @Nested
    @DisplayName("根据编码查询配置组")
    class GetConfigGroupByCodeTests {

        @Test
        @DisplayName("成功根据编码查询配置组")
        void shouldReturnConfigGroupByCode() {
            // Given
            when(configGroupRepository.findByCode("system")).thenReturn(Optional.of(testGroup));
            when(configRepository.countByGroupId("1")).thenReturn(0L);

            // When
            ConfigGroupResponse result = configGroupService.getConfigGroupByCode("system");

            // Then
            assertThat(result.code()).isEqualTo("system");
            assertThat(result.name()).isEqualTo("系统配置");
        }

        @Test
        @DisplayName("根据编码查询不存在的配置组抛出异常")
        void shouldThrowExceptionWhenConfigGroupNotFoundByCode() {
            // Given
            when(configGroupRepository.findByCode("notexist")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configGroupService.getConfigGroupByCode("notexist"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("配置组不存在");
        }
    }

    @Nested
    @DisplayName("创建配置组")
    class CreateConfigGroupTests {

        @Test
        @DisplayName("成功创建配置组")
        void shouldCreateConfigGroup() {
            // Given
            ConfigGroupCreateRequest req = new ConfigGroupCreateRequest(
                    "数据库配置",
                    "database",
                    "Database",
                    2,
                    "数据库相关配置"
            );
            when(configGroupRepository.existsByCode("database")).thenReturn(false);
            when(configGroupRepository.save(any(ConfigGroupEntity.class))).thenAnswer(invocation -> {
                ConfigGroupEntity entity = invocation.getArgument(0);
                entity.setId("2");
                entity.setCreateTime(Instant.now());
                entity.setUpdateTime(Instant.now());
                return entity;
            });
            when(configRepository.countByGroupId("2")).thenReturn(0L);

            // When
            ConfigGroupResponse result = configGroupService.createConfigGroup(req);

            // Then
            assertThat(result.name()).isEqualTo("数据库配置");
            assertThat(result.code()).isEqualTo("database");
            assertThat(result.sortOrder()).isEqualTo(2);
            verify(configGroupRepository).save(any(ConfigGroupEntity.class));
            verify(logService).log(any(LogEntry.class));
        }

        @Test
        @DisplayName("创建重复编码的配置组抛出异常")
        void shouldThrowExceptionWhenCreatingDuplicateCode() {
            // Given
            ConfigGroupCreateRequest req = new ConfigGroupCreateRequest(
                    "系统配置",
                    "system",
                    "Settings",
                    1,
                    "系统相关配置"
            );
            when(configGroupRepository.existsByCode("system")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> configGroupService.createConfigGroup(req))
                    .isInstanceOf(BizException.class)
                    .hasMessage("配置组编码已存在");
        }
    }

    @Nested
    @DisplayName("更新配置组")
    class UpdateConfigGroupTests {

        @Test
        @DisplayName("成功更新配置组")
        void shouldUpdateConfigGroup() {
            // Given
            ConfigGroupUpdateRequest req = new ConfigGroupUpdateRequest(
                    "系统配置（已更新）",
                    "Settings2",
                    2,
                    "更新后的描述"
            );
            when(configGroupRepository.findById("1")).thenReturn(Optional.of(testGroup));
            when(configGroupRepository.save(any(ConfigGroupEntity.class))).thenReturn(testGroup);
            when(configRepository.countByGroupId("1")).thenReturn(0L);

            // When
            ConfigGroupResponse result = configGroupService.updateConfigGroup("1", req);

            // Then
            assertThat(result.name()).isEqualTo("系统配置（已更新）");
            assertThat(result.icon()).isEqualTo("Settings2");
            verify(configGroupRepository).save(any(ConfigGroupEntity.class));
            verify(logService).log(any(LogEntry.class));
        }

        @Test
        @DisplayName("更新不存在的配置组抛出异常")
        void shouldThrowExceptionWhenUpdatingNonExistentConfigGroup() {
            // Given
            ConfigGroupUpdateRequest req = new ConfigGroupUpdateRequest(
                    "新名称",
                    "Icon",
                    1,
                    "新描述"
            );
            when(configGroupRepository.findById("999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configGroupService.updateConfigGroup("999", req))
                    .isInstanceOf(BizException.class)
                    .hasMessage("配置组不存在");
        }
    }

    @Nested
    @DisplayName("删除配置组")
    class DeleteConfigGroupTests {

        @Test
        @DisplayName("成功删除配置组")
        void shouldDeleteConfigGroup() {
            // Given
            when(configGroupRepository.findById("1")).thenReturn(Optional.of(testGroup));
            when(configRepository.countByGroupId("1")).thenReturn(0L);
            doNothing().when(configGroupRepository).deleteById("1");

            // When
            configGroupService.deleteConfigGroup("1");

            // Then
            verify(configGroupRepository).deleteById("1");
            verify(logService).log(any(LogEntry.class));
        }

        @Test
        @DisplayName("删除包含配置项的配置组抛出异常")
        void shouldThrowExceptionWhenDeletingConfigGroupWithConfigs() {
            // Given
            when(configGroupRepository.findById("1")).thenReturn(Optional.of(testGroup));
            when(configRepository.countByGroupId("1")).thenReturn(5L);

            // When & Then
            assertThatThrownBy(() -> configGroupService.deleteConfigGroup("1"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("该配置组下存在配置项，无法删除");
        }

        @Test
        @DisplayName("删除不存在的配置组抛出异常")
        void shouldThrowExceptionWhenDeletingNonExistentConfigGroup() {
            // Given
            when(configGroupRepository.findById("999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configGroupService.deleteConfigGroup("999"))
                    .isInstanceOf(BizException.class)
                    .hasMessage("配置组不存在");
        }
    }

    @Nested
    @DisplayName("更新配置组状态")
    class UpdateConfigGroupStatusTests {

        @Test
        @DisplayName("成功更新配置组状态")
        void shouldUpdateConfigGroupStatus() {
            // Given
            when(configGroupRepository.findById("1")).thenReturn(Optional.of(testGroup));
            when(configGroupRepository.save(any(ConfigGroupEntity.class))).thenReturn(testGroup);

            // When
            configGroupService.updateConfigGroupStatus("1", 0);

            // Then
            assertThat(testGroup.getStatus()).isEqualTo(0);
            verify(configGroupRepository).save(testGroup);
            verify(logService).log(any(LogEntry.class));
        }

        @Test
        @DisplayName("更新不存在的配置组状态抛出异常")
        void shouldThrowExceptionWhenUpdatingNonExistentConfigGroupStatus() {
            // Given
            when(configGroupRepository.findById("999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configGroupService.updateConfigGroupStatus("999", 0))
                    .isInstanceOf(BizException.class)
                    .hasMessage("配置组不存在");
        }
    }

    @Nested
    @DisplayName("查询启用的配置组")
    class GetActiveConfigGroupsTests {

        @Test
        @DisplayName("成功查询启用的配置组列表")
        void shouldReturnActiveConfigGroups() {
            // Given
            ConfigGroupEntity group2 = new ConfigGroupEntity();
            group2.setId("2");
            group2.setName("数据库配置");
            group2.setCode("database");
            group2.setStatus(1);
            group2.setSortOrder(2);
            group2.setCreateTime(Instant.now());
            group2.setUpdateTime(Instant.now());

            when(configGroupRepository.findByStatusOrderBySortOrderAsc(1))
                    .thenReturn(List.of(testGroup, group2));
            when(configRepository.countByGroupId(any())).thenReturn(0L);

            // When
            List<ConfigGroupResponse> result = configGroupService.getActiveConfigGroups();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("系统配置");
            assertThat(result.get(1).name()).isEqualTo("数据库配置");
        }
    }
}
