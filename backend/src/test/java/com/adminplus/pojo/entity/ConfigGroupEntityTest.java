package com.adminplus.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConfigGroupEntity 测试类
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@DisplayName("ConfigGroupEntity Unit Tests")
class ConfigGroupEntityTest {

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have default sortOrder = 0")
        void defaultSortOrder_ShouldBeZero() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();

            // Then
            assertThat(entity.getSortOrder()).isZero();
        }

        @Test
        @DisplayName("should have default status = 1")
        void defaultStatus_ShouldBeOne() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();

            // Then
            assertThat(entity.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("should have default deleted = false from BaseEntity")
        void defaultDeleted_ShouldBeFalse() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();

            // Then
            assertThat(entity.getDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("should set and get name")
        void name_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String name = "系统配置";

            // When
            entity.setName(name);

            // Then
            assertThat(entity.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("should set and get code")
        void code_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String code = "system_config";

            // When
            entity.setCode(code);

            // Then
            assertThat(entity.getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("should set and get icon")
        void icon_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String icon = "Settings";

            // When
            entity.setIcon(icon);

            // Then
            assertThat(entity.getIcon()).isEqualTo(icon);
        }

        @Test
        @DisplayName("should set and get sortOrder")
        void sortOrder_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            Integer sortOrder = 10;

            // When
            entity.setSortOrder(sortOrder);

            // Then
            assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
        }

        @Test
        @DisplayName("should set and get description")
        void description_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String description = "系统相关配置项";

            // When
            entity.setDescription(description);

            // Then
            assertThat(entity.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("should set and get status")
        void status_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            Integer status = 0;

            // When
            entity.setStatus(status);

            // Then
            assertThat(entity.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("should set and get all fields together")
        void allFields_ShouldBeSetAndGet() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String name = "业务配置";
            String code = "business_config";
            String icon = "Briefcase";
            Integer sortOrder = 5;
            String description = "业务相关配置项";
            Integer status = 1;

            // When
            entity.setName(name);
            entity.setCode(code);
            entity.setIcon(icon);
            entity.setSortOrder(sortOrder);
            entity.setDescription(description);
            entity.setStatus(status);

            // Then
            assertThat(entity.getName()).isEqualTo(name);
            assertThat(entity.getCode()).isEqualTo(code);
            assertThat(entity.getIcon()).isEqualTo(icon);
            assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
            assertThat(entity.getDescription()).isEqualTo(description);
            assertThat(entity.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should inherit id field from BaseEntity")
        void shouldInheritIdFromBaseEntity() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String id = "test-id-123";

            // When
            entity.setId(id);

            // Then
            assertThat(entity.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("should inherit createUser field from BaseEntity")
        void shouldInheritCreateUserFromBaseEntity() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String createUser = "admin";

            // When
            entity.setCreateUser(createUser);

            // Then
            assertThat(entity.getCreateUser()).isEqualTo(createUser);
        }

        @Test
        @DisplayName("should inherit updateUser field from BaseEntity")
        void shouldInheritUpdateUserFromBaseEntity() {
            // Given
            ConfigGroupEntity entity = new ConfigGroupEntity();
            String updateUser = "admin";

            // When
            entity.setUpdateUser(updateUser);

            // Then
            assertThat(entity.getUpdateUser()).isEqualTo(updateUser);
        }
    }
}
