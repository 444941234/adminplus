package com.adminplus.controller;

import com.adminplus.pojo.dto.req.FormTemplateReq;
import com.adminplus.pojo.dto.resp.FormTemplateResp;
import com.adminplus.service.FormTemplateService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FormTemplateController 测试类
 *
 * @author AdminPlus
 * @since 2026-04-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FormTemplateController Unit Tests")
class FormTemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FormTemplateService formTemplateService;

    @InjectMocks
    private FormTemplateController formTemplateController;

    private ObjectMapper objectMapper;
    private FormTemplateResp testTemplate;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(formTemplateController)
                .setValidator(validator)
                .build();
        objectMapper = TestJacksonConfig.createObjectMapper();

        testTemplate = new FormTemplateResp(
            "template-001",
            "请假申请表",
            "leave_form",
            "leave",
            "请假申请表单模板",
            "{\"fields\":[{\"name\":\"startDate\",\"label\":\"开始日期\",\"type\":\"date\"}]}",
            1,
            Instant.now(),
            Instant.now()
        );
    }

    @Nested
    @DisplayName("getTemplates Tests")
    class GetTemplatesTests {

        @Test
        @DisplayName("should return all templates")
        void getAllTemplates_Success() throws Exception {
            when(formTemplateService.getAllTemplates()).thenReturn(List.of(testTemplate));

            mockMvc.perform(get("/v1/form-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("template-001"));

            verify(formTemplateService).getAllTemplates();
        }

        @Test
        @DisplayName("should return enabled templates")
        void getEnabledTemplates_Success() throws Exception {
            when(formTemplateService.getEnabledTemplates()).thenReturn(List.of(testTemplate));

            mockMvc.perform(get("/v1/form-templates/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("template-001"));

            verify(formTemplateService).getEnabledTemplates();
        }

        @Test
        @DisplayName("should return templates by category")
        void getTemplatesByCategory_Success() throws Exception {
            when(formTemplateService.getTemplatesByCategory("leave")).thenReturn(List.of(testTemplate));

            mockMvc.perform(get("/v1/form-templates/category/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].category").value("leave"));

            verify(formTemplateService).getTemplatesByCategory("leave");
        }

        @Test
        @DisplayName("should return template by id")
        void getTemplateById_Success() throws Exception {
            when(formTemplateService.getTemplateById("template-001")).thenReturn(testTemplate);

            mockMvc.perform(get("/v1/form-templates/template-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("template-001"));

            verify(formTemplateService).getTemplateById("template-001");
        }

        @Test
        @DisplayName("should return error when template not found by id")
        void getTemplateById_NotFound() throws Exception {
            when(formTemplateService.getTemplateById("non-existent")).thenReturn(null);

            mockMvc.perform(get("/v1/form-templates/non-existent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("should return template by code")
        void getTemplateByCode_Success() throws Exception {
            when(formTemplateService.getTemplateByCode("leave_form")).thenReturn(testTemplate);

            mockMvc.perform(get("/v1/form-templates/code/leave_form"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateCode").value("leave_form"));

            verify(formTemplateService).getTemplateByCode("leave_form");
        }

        @Test
        @DisplayName("should check code exists")
        void checkCodeExists_Success() throws Exception {
            when(formTemplateService.existsByCode("leave_form")).thenReturn(true);

            mockMvc.perform(get("/v1/form-templates/exists/leave_form"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

            verify(formTemplateService).existsByCode("leave_form");
        }
    }

    @Nested
    @DisplayName("createTemplate Tests")
    class CreateTemplateTests {

        @Test
        @DisplayName("should create template successfully")
        void createTemplate_Success() throws Exception {
            when(formTemplateService.createTemplate(any(FormTemplateReq.class))).thenReturn(testTemplate);

            mockMvc.perform(post("/v1/form-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "templateName": "请假申请表",
                            "templateCode": "leave_form",
                            "category": "leave",
                            "description": "请假申请表单模板",
                            "status": 1,
                            "formConfig": "{}"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateCode").value("leave_form"));

            verify(formTemplateService).createTemplate(any(FormTemplateReq.class));
        }

        @Test
        @DisplayName("should return 400 when template code already exists")
        void createTemplate_CodeExists() throws Exception {
            when(formTemplateService.createTemplate(any(FormTemplateReq.class)))
                .thenThrow(new IllegalArgumentException("模板标识已存在"));

            mockMvc.perform(post("/v1/form-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "templateName": "请假申请表",
                            "templateCode": "leave_form",
                            "category": "leave",
                            "status": 1,
                            "formConfig": "{}"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("updateTemplate Tests")
    class UpdateTemplateTests {

        @Test
        @DisplayName("should update template successfully")
        void updateTemplate_Success() throws Exception {
            when(formTemplateService.updateTemplate(anyString(), any(FormTemplateReq.class)))
                .thenReturn(testTemplate);

            mockMvc.perform(put("/v1/form-templates/template-001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "templateName": "请假申请表（更新）",
                            "templateCode": "leave_form",
                            "category": "leave",
                            "status": 1,
                            "formConfig": "{}"
                        }
                        """))
                .andExpect(status().isOk());

            verify(formTemplateService).updateTemplate(anyString(), any(FormTemplateReq.class));
        }

        @Test
        @DisplayName("should return 404 when template not found")
        void updateTemplate_NotFound() throws Exception {
            when(formTemplateService.updateTemplate(anyString(), any(FormTemplateReq.class)))
                .thenThrow(new IllegalArgumentException("表单模板不存在"));

            mockMvc.perform(put("/v1/form-templates/non-existent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "templateName": "请假申请表",
                            "templateCode": "leave_form",
                            "category": "leave",
                            "status": 1,
                            "formConfig": "{}"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
        }
    }

    @Nested
    @DisplayName("deleteTemplate Tests")
    class DeleteTemplateTests {

        @Test
        @DisplayName("should delete template successfully")
        void deleteTemplate_Success() throws Exception {
            mockMvc.perform(delete("/v1/form-templates/template-001"))
                .andExpect(status().isOk());

            verify(formTemplateService).deleteTemplate("template-001");
        }

        @Test
        @DisplayName("should return 404 when template not found")
        void deleteTemplate_NotFound() throws Exception {
            org.mockito.Mockito.doThrow(new IllegalArgumentException("表单模板不存在"))
                .when(formTemplateService).deleteTemplate("non-existent");

            mockMvc.perform(delete("/v1/form-templates/non-existent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
        }
    }
}