package com.adminplus.controller;

import com.adminplus.pojo.dto.req.DictCreateReq;
import com.adminplus.pojo.dto.req.DictUpdateReq;
import com.adminplus.pojo.dto.resp.DictResp;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.service.DictService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DictController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DictController Unit Tests")
class DictControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DictService dictService;

    @InjectMocks
    private DictController dictController;

    private ObjectMapper objectMapper;
    private DictResp testDict;
    private DictCreateReq createReq;
    private DictUpdateReq updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dictController).build();
        objectMapper = new ObjectMapper();
        testDict = new DictResp(
                "dict-001", "status", "状态字典", 1, "状态相关字典",
                Instant.now(), Instant.now()
        );
        createReq = new DictCreateReq("status", "状态字典", "状态相关字典");
        updateReq = new DictUpdateReq("状态字典", 1, "状态相关字典");
    }

    @Nested
    @DisplayName("getDictList Tests")
    class GetDictListTests {

        @Test
        @DisplayName("should return dict list")
        void getDictList_ShouldReturnDictList() throws Exception {
            // Given
            PageResultResp<DictResp> pageResult = new PageResultResp<>(List.of(testDict), 1L, 1, 10);
            when(dictService.getDictList(1, 10, null)).thenReturn(pageResult);

            // When & Then
            mockMvc.perform(get("/v1/sys/dicts")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].dictName").value("状态字典"));

            verify(dictService).getDictList(1, 10, null);
        }
    }

    @Nested
    @DisplayName("getDictById Tests")
    class GetDictByIdTests {

        @Test
        @DisplayName("should return dict by id")
        void getDictById_ShouldReturnDict() throws Exception {
            // Given
            when(dictService.getDictById("dict-001")).thenReturn(testDict);

            // When & Then
            mockMvc.perform(get("/v1/sys/dicts/dict-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.dictName").value("状态字典"));

            verify(dictService).getDictById("dict-001");
        }
    }

    @Nested
    @DisplayName("createDict Tests")
    class CreateDictTests {

        @Test
        @DisplayName("should create dict")
        void createDict_ShouldCreateDict() throws Exception {
            // Given
            when(dictService.createDict(any(DictCreateReq.class))).thenReturn(testDict);

            // When & Then
            mockMvc.perform(post("/v1/sys/dicts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictService).createDict(any(DictCreateReq.class));
        }
    }

    @Nested
    @DisplayName("updateDict Tests")
    class UpdateDictTests {

        @Test
        @DisplayName("should update dict")
        void updateDict_ShouldUpdateDict() throws Exception {
            // Given
            when(dictService.updateDict(anyString(), any(DictUpdateReq.class))).thenReturn(testDict);

            // When & Then
            mockMvc.perform(put("/v1/sys/dicts/dict-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictService).updateDict(anyString(), any(DictUpdateReq.class));
        }
    }

    @Nested
    @DisplayName("deleteDict Tests")
    class DeleteDictTests {

        @Test
        @DisplayName("should delete dict")
        void deleteDict_ShouldDeleteDict() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/dicts/dict-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictService).deleteDict("dict-001");
        }
    }

    @Nested
    @DisplayName("updateDictStatus Tests")
    class UpdateDictStatusTests {

        @Test
        @DisplayName("should update dict status")
        void updateDictStatus_ShouldUpdateStatus() throws Exception {
            // When & Then
            mockMvc.perform(put("/v1/sys/dicts/dict-001/status")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictService).updateDictStatus("dict-001", 0);
        }
    }
}