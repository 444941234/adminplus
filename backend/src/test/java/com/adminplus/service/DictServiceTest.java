package com.adminplus.service;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.dto.request.DictCreateRequest;
import com.adminplus.pojo.dto.request.DictUpdateRequest;
import com.adminplus.pojo.dto.response.DictResponse;
import com.adminplus.pojo.entity.DictEntity;
import com.adminplus.repository.DictItemRepository;
import com.adminplus.repository.DictRepository;
import com.adminplus.service.impl.DictServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DictService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DictService Unit Tests")
class DictServiceTest {

    @Mock
    private DictRepository dictRepository;

    @Mock
    private DictItemRepository dictItemRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private DictServiceImpl dictService;

    private DictEntity testDict;

    @BeforeEach
    void setUp() {
        testDict = new DictEntity();
        testDict.setId("dict-001");
        testDict.setDictType("STATUS");
        testDict.setDictName("Status Dictionary");
        testDict.setStatus(1);
        testDict.setRemark("Status options");
    }

    @Nested
    @DisplayName("getDictById Tests")
    class GetDictByIdTests {

        @Test
        @DisplayName("should return dict when exists")
        void getDictById_WhenExists_ShouldReturnDict() {
            // Given
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));

            // When
            DictResponse result = dictService.getDictById("dict-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.dictType()).isEqualTo("STATUS");
        }

        @Test
        @DisplayName("should throw exception when dict not found")
        void getDictById_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictService.getDictById("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }

    @Nested
    @DisplayName("getDictByType Tests")
    class GetDictByTypeTests {

        @Test
        @DisplayName("should return dict when type exists")
        void getDictByType_WhenExists_ShouldReturnDict() {
            // Given
            when(dictRepository.findByDictType("STATUS")).thenReturn(Optional.of(testDict));

            // When
            DictResponse result = dictService.getDictByType("STATUS");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.dictType()).isEqualTo("STATUS");
        }

        @Test
        @DisplayName("should throw exception when type not found")
        void getDictByType_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictRepository.findByDictType("NON_EXISTENT")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictService.getDictByType("NON_EXISTENT"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }

    @Nested
    @DisplayName("createDict Tests")
    class CreateDictTests {

        @Test
        @DisplayName("should create dict successfully")
        void createDict_ShouldCreateDict() {
            // Given
            DictCreateRequest req = new DictCreateRequest("GENDER", "Gender Dictionary", "Gender options");
            when(dictRepository.existsByDictType("GENDER")).thenReturn(false);
            when(dictRepository.save(any())).thenReturn(testDict);

            // When
            DictResponse result = dictService.createDict(req);

            // Then
            assertThat(result).isNotNull();
            verify(dictRepository).save(any(DictEntity.class));
        }

        @Test
        @DisplayName("should throw exception when dict type exists")
        void createDict_WhenTypeExists_ShouldThrowException() {
            // Given
            DictCreateRequest req = new DictCreateRequest("STATUS", "Status Dictionary", "Status options");
            when(dictRepository.existsByDictType("STATUS")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> dictService.createDict(req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典类型已存在");
        }
    }

    @Nested
    @DisplayName("updateDict Tests")
    class UpdateDictTests {

        @Test
        @DisplayName("should update dict successfully")
        void updateDict_ShouldUpdateDict() {
            // Given
            DictUpdateRequest req = new DictUpdateRequest("Updated Dictionary", 1, "Updated remark");
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictRepository.save(any())).thenReturn(testDict);

            // When
            DictResponse result = dictService.updateDict("dict-001", req);

            // Then
            assertThat(result).isNotNull();
            verify(dictRepository).save(any(DictEntity.class));
        }

        @Test
        @DisplayName("should throw exception when dict not found")
        void updateDict_WhenNotFound_ShouldThrowException() {
            // Given
            DictUpdateRequest req = new DictUpdateRequest("Updated Dictionary", 1, "Updated remark");
            when(dictRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictService.updateDict("non-existent", req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }

    @Nested
    @DisplayName("deleteDict Tests")
    class DeleteDictTests {

        @Test
        @DisplayName("should delete dict successfully")
        void deleteDict_ShouldDeleteDict() {
            // Given
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictRepository.save(any())).thenReturn(testDict);

            // When
            dictService.deleteDict("dict-001");

            // Then
            verify(dictRepository).save(any(DictEntity.class));
        }

        @Test
        @DisplayName("should throw exception when dict not found")
        void deleteDict_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictService.deleteDict("non-existent"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }

    @Nested
    @DisplayName("updateDictStatus Tests")
    class UpdateDictStatusTests {

        @Test
        @DisplayName("should update status successfully")
        void updateDictStatus_ShouldUpdateStatus() {
            // Given
            when(dictRepository.findById("dict-001")).thenReturn(Optional.of(testDict));
            when(dictRepository.save(any())).thenReturn(testDict);

            // When
            dictService.updateDictStatus("dict-001", 0);

            // Then
            verify(dictRepository).save(any(DictEntity.class));
        }

        @Test
        @DisplayName("should throw exception when dict not found")
        void updateDictStatus_WhenNotFound_ShouldThrowException() {
            // Given
            when(dictRepository.findById("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> dictService.updateDictStatus("non-existent", 0))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("字典不存在");
        }
    }
}