package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.DictItemCreateRequest;
import com.adminplus.pojo.dto.request.DictItemUpdateRequest;
import com.adminplus.pojo.dto.response.DictItemResponse;
import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.pojo.entity.DictItemEntity;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.service.impl.DictItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DictItemService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DictItemService Unit Tests")
class DictItemServiceTest {

    @Mock
    private DictItemRepository dictItemRepository;

    @Mock
    private DictRepository dictRepository;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private DictItemServiceImpl dictItemService;

    private DictEntity testDict;
    private DictItemEntity testItem;
    private DictItemResponse testItemResponse;

    @BeforeEach
    void setUp() {
        testDict = new DictEntity();
        testDict.setId("dict-001");
        testDict.setDictType("STATUS");
        testDict.setDictName("Status Dictionary");
        testDict.setStatus(1);

        testItem = new DictItemEntity();
        testItem.setId("item-001");
        testItem.setDictId("dict-001");
        testItem.setLabel("Active");
        testItem.setValue("1");
        testItem.setSortOrder(1);
        testItem.setStatus(1);

        testItemResponse = new DictItemResponse(
                testItem.getId(),
                testItem.getDictId(),
                testDict.getDictType(),
                "0",
                testItem.getLabel(),
                testItem.getValue(),
                testItem.getSortOrder(),
                testItem.getStatus(),
                null,
                null,
                testItem.getCreateTime(),
                testItem.getUpdateTime()
        );

        // Mock conversionService
        lenient().when(conversionService.convert(any(DictItemEntity.class), eq(DictItemResponse.class)))
                .thenReturn(testItemResponse);
    }

    @Nested
    @DisplayName("getDictItemById Tests")
    class GetDictItemByIdTests {

        @Test
        @DisplayName("should return dict item when exists")
        void getDictItemById_WhenExists_ShouldReturnItem() {
            // Given
            when(dictItemRepository.findById("item-001")).thenReturn(Optional.of(testItem));
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));

            // When
            DictItemResponse result = dictItemService.getDictItemById("item-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.label()).isEqualTo("Active");
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void getDictItemById_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictItemRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictItemService.getDictItemById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典项不存在");
        }
    }

    @Nested
    @DisplayName("getDictItemsByDictId Tests")
    class GetDictItemsByDictIdTests {

        @Test
        @DisplayName("should return items for dict")
        void getDictItemsByDictId_ShouldReturnItems() {
            // Given
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictItemRepository.findByDictIdOrderBySortOrderAsc("dict-001")).thenReturn(List.of(testItem));

            // When
            List<DictItemResponse> result = dictItemService.getDictItemsByDictId("dict-001");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no items")
        void getDictItemsByDictId_WhenNoItems_ShouldReturnEmptyList() {
            // Given
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictItemRepository.findByDictIdOrderBySortOrderAsc("dict-001")).thenReturn(List.of());

            // When
            List<DictItemResponse> result = dictItemService.getDictItemsByDictId("dict-001");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDictItemsByType Tests")
    class GetDictItemsByTypeTests {

        @Test
        @DisplayName("should return items for dict type")
        void getDictItemsByType_ShouldReturnItems() {
            // Given
            when(dictRepository.findByDictType("STATUS")).thenReturn(Optional.of(testDict));
            when(dictItemRepository.findByDictIdAndStatusOrderBySortOrderAsc("dict-001", 1))
                    .thenReturn(List.of(testItem));

            // When
            List<DictItemResponse> result = dictItemService.getDictItemsByType("STATUS");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw exception when dict type not found")
        void getDictItemsByType_WhenDictNotFound_ShouldThrowException() {
            // Given
            when(dictRepository.findByDictType("NON_EXISTENT")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictItemService.getDictItemsByType("NON_EXISTENT"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }

    @Nested
    @DisplayName("createDictItem Tests")
    class CreateDictItemTests {

        @Test
        @DisplayName("should create dict item successfully")
        void createDictItem_ShouldCreateItem() {
            // Given
            DictItemCreateRequest req = new DictItemCreateRequest(
                    "dict-001", null, "Inactive", "0", 2, 1, "Inactive status"
            );
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictItemRepository.save(any())).thenReturn(testItem);

            // When
            DictItemResponse result = dictItemService.createDictItem(req);

            // Then
            assertThat(result).isNotNull();
            verify(dictItemRepository).save(any(DictItemEntity.class));
        }

        @Test
        @DisplayName("should throw exception when dict not found")
        void createDictItem_WhenDictNotFound_ShouldThrowException() {
            // Given
            DictItemCreateRequest req = new DictItemCreateRequest(
                    "non-existent", null, "Test", "test", 1, 1, null
            );
            when(dictRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictItemService.createDictItem(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }

    @Nested
    @DisplayName("updateDictItem Tests")
    class UpdateDictItemTests {

        @Test
        @DisplayName("should update dict item successfully")
        void updateDictItem_ShouldUpdateItem() {
            // Given
            DictItemUpdateRequest req = new DictItemUpdateRequest(
                    null, "Updated Label", "2", 2, 1, "Updated"
            );
            when(dictItemRepository.findById("item-001")).thenReturn(Optional.of(testItem));
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictItemRepository.save(any())).thenReturn(testItem);

            // When
            DictItemResponse result = dictItemService.updateDictItem("item-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(dictItemRepository).save(any(DictItemEntity.class));
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void updateDictItem_WhenItemNotFound_ShouldThrowException() {
            // Given
            DictItemUpdateRequest req = new DictItemUpdateRequest(
                    null, "Updated", null, null, null, null
            );
            when(dictItemRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictItemService.updateDictItem("non-existent", req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典项不存在");
        }
    }

    @Nested
    @DisplayName("deleteDictItem Tests")
    class DeleteDictItemTests {

        @Test
        @DisplayName("should delete dict item without children")
        void deleteDictItem_WithoutChildren_ShouldDelete() {
            // Given
            testItem.setChildren(List.of());
            when(dictItemRepository.findById("item-001")).thenReturn(Optional.of(testItem));

            // When
            dictItemService.deleteDictItem("item-001");

            // Then
            verify(dictItemRepository).delete(testItem);
        }

        @Test
        @DisplayName("should throw exception when item has children")
        void deleteDictItem_WithChildren_ShouldThrowException() {
            // Given
            DictItemEntity childItem = new DictItemEntity();
            childItem.setId("child-001");
            testItem.setChildren(List.of(childItem));
            when(dictItemRepository.findById("item-001")).thenReturn(Optional.of(testItem));

            // When & Then
            assertThatThrownBy(() -> dictItemService.deleteDictItem("item-001"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("存在子节点");
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void deleteDictItem_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictItemRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictItemService.deleteDictItem("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典项不存在");
        }
    }

    @Nested
    @DisplayName("updateDictItemStatus Tests")
    class UpdateDictItemStatusTests {

        @Test
        @DisplayName("should update status successfully")
        void updateDictItemStatus_ShouldUpdateStatus() {
            // Given
            when(dictItemRepository.findById("item-001")).thenReturn(Optional.of(testItem));
            when(dictItemRepository.save(any())).thenReturn(testItem);

            // When
            dictItemService.updateDictItemStatus("item-001", 0);

            // Then
            verify(dictItemRepository).save(any(DictItemEntity.class));
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void updateDictItemStatus_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictItemRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictItemService.updateDictItemStatus("non-existent", 0))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典项不存在");
        }
    }
}