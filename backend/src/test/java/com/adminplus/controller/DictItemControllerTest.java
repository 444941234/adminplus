package com.adminplus.controller;

import com.adminplus.pojo.dto.request.DictItemCreateRequest;
import com.adminplus.pojo.dto.request.DictItemUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.service.DictItemService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DictItemController 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DictItemController Unit Tests")
class DictItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DictItemService dictItemService;

    @InjectMocks
    private DictItemController dictItemController;

    private ObjectMapper objectMapper;
    private DictItemResponse testDictItem;
    private DictItemCreateRequest createReq;
    private DictItemUpdateRequest updateReq;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dictItemController).build();
        objectMapper = TestJacksonConfig.createObjectMapper();
        testDictItem = new DictItemResponse(
                "item-001", "dict-001", "status", null,
                "启用", "1", 1, 1, null,
                List.of(), Instant.now(), Instant.now()
        );
        createReq = new DictItemCreateRequest("dict-001", null, "启用", "1", 1, 1, null);
        updateReq = new DictItemUpdateRequest(null, "启用", "1", 1, 1, null);
    }

    @Nested
    @DisplayName("getDictItems Tests")
    class GetDictItemsTests {

        @Test
        @DisplayName("should return dict items")
        void getDictItems_ShouldReturnItems() throws Exception {
            // Given
            when(dictItemService.getDictItemsByDictId("dict-001")).thenReturn(List.of(testDictItem));

            // When & Then
            mockMvc.perform(get("/v1/sys/dicts/dict-001/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].label").value("启用"));

            verify(dictItemService).getDictItemsByDictId("dict-001");
        }
    }

    @Nested
    @DisplayName("getDictItemById Tests")
    class GetDictItemByIdTests {

        @Test
        @DisplayName("should return dict item by id")
        void getDictItemById_ShouldReturnItem() throws Exception {
            // Given
            when(dictItemService.getDictItemById("item-001")).thenReturn(testDictItem);

            // When & Then
            mockMvc.perform(get("/v1/sys/dicts/dict-001/items/item-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.label").value("启用"));

            verify(dictItemService).getDictItemById("item-001");
        }
    }

    @Nested
    @DisplayName("createDictItem Tests")
    class CreateDictItemTests {

        @Test
        @DisplayName("should create dict item")
        void createDictItem_ShouldCreateItem() throws Exception {
            // Given
            when(dictItemService.createDictItem(any(DictItemCreateRequest.class))).thenReturn(testDictItem);

            // When & Then
            mockMvc.perform(post("/v1/sys/dicts/dict-001/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictItemService).createDictItem(any(DictItemCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("updateDictItem Tests")
    class UpdateDictItemTests {

        @Test
        @DisplayName("should update dict item")
        void updateDictItem_ShouldUpdateItem() throws Exception {
            // Given
            when(dictItemService.updateDictItem(anyString(), any(DictItemUpdateRequest.class))).thenReturn(testDictItem);

            // When & Then
            mockMvc.perform(put("/v1/sys/dicts/dict-001/items/item-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictItemService).updateDictItem(anyString(), any(DictItemUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("deleteDictItem Tests")
    class DeleteDictItemTests {

        @Test
        @DisplayName("should delete dict item")
        void deleteDictItem_ShouldDeleteItem() throws Exception {
            // When & Then
            mockMvc.perform(delete("/v1/sys/dicts/dict-001/items/item-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictItemService).deleteDictItem("item-001");
        }
    }

    @Nested
    @DisplayName("updateDictItemStatus Tests")
    class UpdateDictItemStatusTests {

        @Test
        @DisplayName("should update dict item status")
        void updateDictItemStatus_ShouldUpdateStatus() throws Exception {
            // When & Then
            mockMvc.perform(put("/v1/sys/dicts/dict-001/items/item-001/status")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(dictItemService).updateDictItemStatus("item-001", 0);
        }
    }
}