package com.adminplus.controller;

import com.adminplus.pojo.dto.resp.DeptResp;
import com.adminplus.pojo.dto.req.DeptCreateReq;
import com.adminplus.pojo.dto.req.DeptUpdateReq;
import com.adminplus.service.DeptService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DeptController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeptController Unit Tests")
class DeptControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeptService deptService;

    @InjectMocks
    private DeptController deptController;

    private ObjectMapper objectMapper;
    private DeptResp testDept;
    private DeptCreateReq createReq;
    private DeptUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deptController).build();
        objectMapper = new ObjectMapper();
        testDept = new DeptResp("dept-001", null, "技术部", "TECH", "张三", "13800138000", "tech@example.com", 1, 1, null, null, null);
        createReq = new DeptCreateReq(null, "技术部", "TECH", "张三", "13800138000", "tech@example.com", 1, 1);
        updateReq = new DeptUpdateReq(Optional.empty(), Optional.of("技术部"), Optional.of("TECH"), Optional.of("张三"), Optional.of("13800138000"), Optional.of("tech@example.com"), Optional.of(1), Optional.of(1));
    }

    @Nested
    @DisplayName("getDeptTree Tests")
    class GetDeptTreeTests {

        @Test
        @DisplayName("should return dept tree")
        void getDeptTree_ShouldReturnTree() throws Exception {
            // Given
            when(deptService.getDeptTree()).thenReturn(List.of(testDept));

            // When & Then
            mockMvc.perform(get("/v1/sys/depts/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].name").value("技术部"));

            verify(deptService).getDeptTree();
        }
    }

    @Nested
    @DisplayName("getDeptById Tests")
    class GetDeptByIdTests {

        @Test
        @DisplayName("should return dept by id")
        void getDeptById_ShouldReturnDept() throws Exception {
            // Given
            when(deptService.getDeptById("dept-001")).thenReturn(testDept);

            // When & Then
            mockMvc.perform(get("/v1/sys/depts/dept-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("技术部"));

            verify(deptService).getDeptById("dept-001");
        }
    }

    @Nested
    @DisplayName("createDept Tests")
    class CreateDeptTests {

        @Test
        @DisplayName("should create dept")
        void createDept_ShouldCreateDept() throws Exception {
            // Given
            when(deptService.createDept(any(DeptCreateReq.class))).thenReturn(testDept);

            // When & Then
            mockMvc.perform(post("/v1/sys/depts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("技术部"));

            verify(deptService).createDept(any(DeptCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateDept Tests")
    class UpdateDeptTests {

        @Test
        @DisplayName("should update dept")
        void updateDept_ShouldUpdateDept() throws Exception {
            // Given
            when(deptService.updateDept(anyString(), any(DeptUpdateReq.class))).thenReturn(testDept);

            // When & Then
            mockMvc.perform(put("/v1/sys/depts/dept-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(deptService).updateDept(anyString(), any(DeptUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteDept Tests")
    class DeleteDeptTests {

        @Test
        @DisplayName("should delete dept")
        void deleteDept_ShouldDeleteDept() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/depts/dept-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(deptService).deleteDept("dept-001");
        }
    }
}