package com.adminplus.service;

import com.adminplus.pojo.dto.request.FormTemplateRequest;
import com.adminplus.pojo.dto.response.FormTemplateResponse;
import com.adminplus.pojo.entity.FormTemplateEntity;
import com.adminplus.repository.FormTemplateRepository;
import com.adminplus.service.impl.FormTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FormTemplateService 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FormTemplateService Unit Tests")
class FormTemplateServiceTest {

    @Mock
    private FormTemplateRepository templateRepository;

    @InjectMocks
    private FormTemplateServiceImpl templateService;

    private FormTemplateEntity testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new FormTemplateEntity();
        testTemplate.setId("template-001");
        testTemplate.setTemplateName("请假申请表");
        testTemplate.setTemplateCode("leave_request");
        testTemplate.setCategory("hr");
        testTemplate.setDescription("员工请假申请表单");
        testTemplate.setFormConfig("{\"fields\":[]}");
        testTemplate.setStatus(1);
    }

    @Nested
    @DisplayName("getAllTemplates Tests")
    class GetAllTemplatesTests {

        @Test
        @DisplayName("should return all templates")
        void getAllTemplates_ShouldReturnAll() {
            // Given
            when(templateRepository.findAll()).thenReturn(List.of(testTemplate));

            // When
            List<FormTemplateResponse> result = templateService.getAllTemplates();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).templateCode()).isEqualTo("leave_request");
        }

        @Test
        @DisplayName("should return empty list when no templates")
        void getAllTemplates_WhenNone_ShouldReturnEmptyList() {
            // Given
            when(templateRepository.findAll()).thenReturn(List.of());

            // When
            List<FormTemplateResponse> result = templateService.getAllTemplates();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getEnabledTemplates Tests")
    class GetEnabledTemplatesTests {

        @Test
        @DisplayName("should return enabled templates")
        void getEnabledTemplates_ShouldReturnEnabled() {
            // Given
            when(templateRepository.findByStatusOrderByCreateTimeDesc(1))
                    .thenReturn(List.of(testTemplate));

            // When
            List<FormTemplateResponse> result = templateService.getEnabledTemplates();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty list when no enabled templates")
        void getEnabledTemplates_WhenNone_ShouldReturnEmptyList() {
            // Given
            when(templateRepository.findByStatusOrderByCreateTimeDesc(1))
                    .thenReturn(List.of());

            // When
            List<FormTemplateResponse> result = templateService.getEnabledTemplates();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplatesByCategory Tests")
    class GetTemplatesByCategoryTests {

        @Test
        @DisplayName("should return templates by category")
        void getTemplatesByCategory_ShouldReturnByCategory() {
            // Given
            when(templateRepository.findByCategoryAndStatusOrderByCreateTimeDesc("hr", 1))
                    .thenReturn(List.of(testTemplate));

            // When
            List<FormTemplateResponse> result = templateService.getTemplatesByCategory("hr");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).category()).isEqualTo("hr");
        }

        @Test
        @DisplayName("should return empty list when no templates in category")
        void getTemplatesByCategory_WhenNone_ShouldReturnEmptyList() {
            // Given
            when(templateRepository.findByCategoryAndStatusOrderByCreateTimeDesc("finance", 1))
                    .thenReturn(List.of());

            // When
            List<FormTemplateResponse> result = templateService.getTemplatesByCategory("finance");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplateById Tests")
    class GetTemplateByIdTests {

        @Test
        @DisplayName("should return template when exists")
        void getTemplateById_WhenExists_ShouldReturnTemplate() {
            // Given
            when(templateRepository.findById("template-001")).thenReturn(Optional.of(testTemplate));

            // When
            FormTemplateResponse result = templateService.getTemplateById("template-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("template-001");
            assertThat(result.templateName()).isEqualTo("请假申请表");
        }

        @Test
        @DisplayName("should return null when not found")
        void getTemplateById_WhenNotFound_ShouldReturnNull() {
            // Given
            when(templateRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When
            FormTemplateResponse result = templateService.getTemplateById("non-existent");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getTemplateByCode Tests")
    class GetTemplateByCodeTests {

        @Test
        @DisplayName("should return template when code exists")
        void getTemplateByCode_WhenExists_ShouldReturnTemplate() {
            // Given
            when(templateRepository.findByTemplateCode("leave_request"))
                    .thenReturn(Optional.of(testTemplate));

            // When
            FormTemplateResponse result = templateService.getTemplateByCode("leave_request");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.templateCode()).isEqualTo("leave_request");
        }

        @Test
        @DisplayName("should return null when code not found")
        void getTemplateByCode_WhenNotFound_ShouldReturnNull() {
            // Given
            when(templateRepository.findByTemplateCode("non-existent"))
                    .thenReturn(Optional.empty());

            // When
            FormTemplateResponse result = templateService.getTemplateByCode("non-existent");

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("createTemplate Tests")
    class CreateTemplateTests {

        @Test
        @DisplayName("should create template successfully")
        void createTemplate_ShouldCreateTemplate() {
            // Given
            FormTemplateRequest req = new FormTemplateRequest(
                    "报销申请表", "expense_claim", "finance", "费用报销申请", 1, "{}"
            );
            when(templateRepository.existsByTemplateCode("expense_claim")).thenReturn(false);
            when(templateRepository.save(any())).thenAnswer(inv -> {
                FormTemplateEntity entity = inv.getArgument(0);
                entity.setId("new-id");
                return entity;
            });

            // When
            FormTemplateResponse result = templateService.createTemplate(req);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.templateCode()).isEqualTo("expense_claim");
            verify(templateRepository).save(any(FormTemplateEntity.class));
        }

        @Test
        @DisplayName("should throw exception when code already exists")
        void createTemplate_WhenCodeExists_ShouldThrowException() {
            // Given
            FormTemplateRequest req = new FormTemplateRequest(
                    "报销申请表", "leave_request", "finance", "费用报销申请", 1, "{}"
            );
            when(templateRepository.existsByTemplateCode("leave_request")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> templateService.createTemplate(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("表单标识已存在");
        }
    }

    @Nested
    @DisplayName("updateTemplate Tests")
    class UpdateTemplateTests {

        @Test
        @DisplayName("should update template successfully")
        void updateTemplate_ShouldUpdateTemplate() {
            // Given
            FormTemplateRequest req = new FormTemplateRequest(
                    "请假申请表V2", "leave_request", "hr", "更新后的描述", 1, "{\"fields\":[]}"
            );
            when(templateRepository.findById("template-001")).thenReturn(Optional.of(testTemplate));
            when(templateRepository.save(any())).thenReturn(testTemplate);

            // When
            FormTemplateResponse result = templateService.updateTemplate("template-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(templateRepository).save(any(FormTemplateEntity.class));
        }

        @Test
        @DisplayName("should throw exception when template not found")
        void updateTemplate_WhenNotFound_ShouldThrowException() {
            // Given
            FormTemplateRequest req = new FormTemplateRequest(
                    "名称", "code", "cat", "desc", 1, "{}"
            );
            when(templateRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> templateService.updateTemplate("non-existent", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("表单模板不存在");
        }

        @Test
        @DisplayName("should throw exception when new code already used by another")
        void updateTemplate_WhenCodeUsedByOther_ShouldThrowException() {
            // Given
            FormTemplateRequest req = new FormTemplateRequest(
                    "名称", "different_code", "cat", "desc", 1, "{}"
            );
            when(templateRepository.findById("template-001")).thenReturn(Optional.of(testTemplate));
            when(templateRepository.existsByTemplateCode("different_code")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> templateService.updateTemplate("template-001", req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("表单标识已存在");
        }

        @Test
        @DisplayName("should allow updating with same code")
        void updateTemplate_WithSameCode_ShouldSucceed() {
            // Given
            FormTemplateRequest req = new FormTemplateRequest(
                    "请假申请表V2", "leave_request", "hr", "更新描述", 1, "{}"
            );
            when(templateRepository.findById("template-001")).thenReturn(Optional.of(testTemplate));
            when(templateRepository.save(any())).thenReturn(testTemplate);

            // When
            FormTemplateResponse result = templateService.updateTemplate("template-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(templateRepository).save(any(FormTemplateEntity.class));
        }
    }

    @Nested
    @DisplayName("deleteTemplate Tests")
    class DeleteTemplateTests {

        @Test
        @DisplayName("should delete template successfully")
        void deleteTemplate_ShouldDeleteTemplate() {
            // Given
            when(templateRepository.existsById("template-001")).thenReturn(true);
            doNothing().when(templateRepository).deleteById("template-001");

            // When
            templateService.deleteTemplate("template-001");

            // Then
            verify(templateRepository).deleteById("template-001");
        }

        @Test
        @DisplayName("should throw exception when template not found")
        void deleteTemplate_WhenNotFound_ShouldThrowException() {
            // Given
            when(templateRepository.existsById("non-existent")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> templateService.deleteTemplate("non-existent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("表单模板不存在");
        }
    }

    @Nested
    @DisplayName("existsByCode Tests")
    class ExistsByCodeTests {

        @Test
        @DisplayName("should return true when code exists")
        void existsByCode_WhenExists_ShouldReturnTrue() {
            // Given
            when(templateRepository.existsByTemplateCode("leave_request")).thenReturn(true);

            // When
            boolean result = templateService.existsByCode("leave_request");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when code not exists")
        void existsByCode_WhenNotExists_ShouldReturnFalse() {
            // Given
            when(templateRepository.existsByTemplateCode("non-existent")).thenReturn(false);

            // When
            boolean result = templateService.existsByCode("non-existent");

            // Then
            assertThat(result).isFalse();
        }
    }
}