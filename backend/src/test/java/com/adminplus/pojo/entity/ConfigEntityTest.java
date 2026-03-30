package com.adminplus.pojo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConfigEntity 测试类
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@DisplayName("ConfigEntity Unit Tests")
class ConfigEntityTest {

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have default valueType = STRING")
        void defaultValueType_ShouldBeSTRING() {
            // Given
            ConfigEntity entity = new ConfigEntity();

            // Then
            assertThat(entity.getValueType()).isEqualTo("STRING");
        }

        @Test
        @DisplayName("should have default effectType = IMMEDIATE")
        void defaultEffectType_ShouldBeIMMEDIATE() {
            // Given
            ConfigEntity entity = new ConfigEntity();

            // Then
            assertThat(entity.getEffectType()).isEqualTo("IMMEDIATE");
        }

        @Test
        @DisplayName("should have default isRequired = false")
        void defaultIsRequired_ShouldBeFalse() {
            // Given
            ConfigEntity entity = new ConfigEntity();

            // Then
            assertThat(entity.getIsRequired()).isFalse();
        }

        @Test
        @DisplayName("should have default sortOrder = 0")
        void defaultSortOrder_ShouldBeZero() {
            // Given
            ConfigEntity entity = new ConfigEntity();

            // Then
            assertThat(entity.getSortOrder()).isZero();
        }

        @Test
        @DisplayName("should have default status = 1")
        void defaultStatus_ShouldBeOne() {
            // Given
            ConfigEntity entity = new ConfigEntity();

            // Then
            assertThat(entity.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("should have default deleted = false from BaseEntity")
        void defaultDeleted_ShouldBeFalse() {
            // Given
            ConfigEntity entity = new ConfigEntity();

            // Then
            assertThat(entity.getDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("should set and get groupId")
        void groupId_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String groupId = "group-123";

            // When
            entity.setGroupId(groupId);

            // Then
            assertThat(entity.getGroupId()).isEqualTo(groupId);
        }

        @Test
        @DisplayName("should set and get name")
        void name_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String name = "系统名称";

            // When
            entity.setName(name);

            // Then
            assertThat(entity.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("should set and get key")
        void key_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String key = "system.name";

            // When
            entity.setKey(key);

            // Then
            assertThat(entity.getKey()).isEqualTo(key);
        }

        @Test
        @DisplayName("should set and get value")
        void value_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String value = "AdminPlus系统";

            // When
            entity.setValue(value);

            // Then
            assertThat(entity.getValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("should set and get valueType")
        void valueType_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String valueType = "NUMBER";

            // When
            entity.setValueType(valueType);

            // Then
            assertThat(entity.getValueType()).isEqualTo(valueType);
        }

        @Test
        @DisplayName("should set and get effectType")
        void effectType_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String effectType = "RESTART";

            // When
            entity.setEffectType(effectType);

            // Then
            assertThat(entity.getEffectType()).isEqualTo(effectType);
        }

        @Test
        @DisplayName("should set and get defaultValue")
        void defaultValue_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String defaultValue = "默认值";

            // When
            entity.setDefaultValue(defaultValue);

            // Then
            assertThat(entity.getDefaultValue()).isEqualTo(defaultValue);
        }

        @Test
        @DisplayName("should set and get description")
        void description_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String description = "系统名称配置";

            // When
            entity.setDescription(description);

            // Then
            assertThat(entity.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("should set and get isRequired")
        void isRequired_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            Boolean isRequired = true;

            // When
            entity.setIsRequired(isRequired);

            // Then
            assertThat(entity.getIsRequired()).isEqualTo(isRequired);
        }

        @Test
        @DisplayName("should set and get validationRule")
        void validationRule_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            String validationRule = "^[a-zA-Z0-9]{1,50}$";

            // When
            entity.setValidationRule(validationRule);

            // Then
            assertThat(entity.getValidationRule()).isEqualTo(validationRule);
        }

        @Test
        @DisplayName("should set and get sortOrder")
        void sortOrder_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
            Integer sortOrder = 10;

            // When
            entity.setSortOrder(sortOrder);

            // Then
            assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
        }

        @Test
        @DisplayName("should set and get status")
        void status_ShouldBeSetAndGet() {
            // Given
            ConfigEntity entity = new ConfigEntity();
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
            ConfigEntity entity = new ConfigEntity();
            String groupId = "group-456";
            String name = "最大连接数";
            String key = "database.max.connections";
            String value = "100";
            String valueType = "NUMBER";
            String effectType = "MANUAL";
            String defaultValue = "50";
            String description = "数据库最大连接数配置";
            Boolean isRequired = true;
            String validationRule = "[1-1000]";
            Integer sortOrder = 5;
            Integer status = 1;

            // When
            entity.setGroupId(groupId);
            entity.setName(name);
            entity.setKey(key);
            entity.setValue(value);
            entity.setValueType(valueType);
            entity.setEffectType(effectType);
            entity.setDefaultValue(defaultValue);
            entity.setDescription(description);
            entity.setIsRequired(isRequired);
            entity.setValidationRule(validationRule);
            entity.setSortOrder(sortOrder);
            entity.setStatus(status);

            // Then
            assertThat(entity.getGroupId()).isEqualTo(groupId);
            assertThat(entity.getName()).isEqualTo(name);
            assertThat(entity.getKey()).isEqualTo(key);
            assertThat(entity.getValue()).isEqualTo(value);
            assertThat(entity.getValueType()).isEqualTo(valueType);
            assertThat(entity.getEffectType()).isEqualTo(effectType);
            assertThat(entity.getDefaultValue()).isEqualTo(defaultValue);
            assertThat(entity.getDescription()).isEqualTo(description);
            assertThat(entity.getIsRequired()).isEqualTo(isRequired);
            assertThat(entity.getValidationRule()).isEqualTo(validationRule);
            assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
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
            ConfigEntity entity = new ConfigEntity();
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
            ConfigEntity entity = new ConfigEntity();
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
            ConfigEntity entity = new ConfigEntity();
            String updateUser = "admin";

            // When
            entity.setUpdateUser(updateUser);

            // Then
            assertThat(entity.getUpdateUser()).isEqualTo(updateUser);
        }
    }
}
