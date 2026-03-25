package com.adminplus.service;

import com.adminplus.common.properties.AppProperties;
import com.adminplus.service.impl.VirusScanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * VirusScanService 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VirusScanService Unit Tests")
class VirusScanServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Virus virus;

    @Mock
    private AppProperties.Virus.Scan scan;

    @Mock
    private AppProperties.Virus.Scan.Clamav clamav;

    @InjectMocks
    private VirusScanServiceImpl virusScanService;

    @BeforeEach
    void setUp() {
        lenient().when(appProperties.getVirus()).thenReturn(virus);
        lenient().when(virus.getScan()).thenReturn(scan);
        lenient().when(scan.getClamav()).thenReturn(clamav);
        lenient().when(clamav.getHost()).thenReturn("localhost");
        lenient().when(clamav.getPort()).thenReturn(3310);
        lenient().when(scan.getTimeout()).thenReturn(30000);
    }

    @Nested
    @DisplayName("scanFile Tests")
    class ScanFileTests {

        @Test
        @DisplayName("should return true when virus scan is disabled")
        void scanFile_WhenScanDisabled_ShouldReturnTrue() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "content".getBytes());
            when(scan.isEnabled()).thenReturn(false);

            // When
            boolean result = virusScanService.scanFile(file);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true when ClamAV service is not available")
        void scanFile_WhenServiceNotAvailable_ShouldReturnTrue() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "content".getBytes());
            when(scan.isEnabled()).thenReturn(true);
            when(clamav.getHost()).thenReturn("unreachable-host");
            when(clamav.getPort()).thenReturn(9999);

            // When
            boolean result = virusScanService.scanFile(file);

            // Then
            assertThat(result).isTrue(); // Service unavailable, but allows upload
        }
    }

    @Nested
    @DisplayName("isServiceAvailable Tests")
    class IsServiceAvailableTests {

        @Test
        @DisplayName("should return false when scan is disabled")
        void isServiceAvailable_WhenScanDisabled_ShouldReturnFalse() {
            // Given
            when(scan.isEnabled()).thenReturn(false);

            // When
            boolean result = virusScanService.isServiceAvailable();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when ClamAV is unreachable")
        void isServiceAvailable_WhenClamavUnreachable_ShouldReturnFalse() {
            // Given
            when(scan.isEnabled()).thenReturn(true);
            when(clamav.getHost()).thenReturn("unreachable-host");
            when(clamav.getPort()).thenReturn(9999);

            // When
            boolean result = virusScanService.isServiceAvailable();

            // Then
            assertThat(result).isFalse();
        }
    }
}