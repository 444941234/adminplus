package com.adminplus.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileContentValidator 测试类
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
class FileContentValidatorTest {

    @Nested
    class ValidateContentTypeTests {
        @Test
        void validateContentType_WithNullFile_ShouldReturnFalse() {
            // When
            boolean result = FileContentValidator.validateContentType(null, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateContentType_WithNullContentType_ShouldReturnFalse() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{});

            // When
            boolean result = FileContentValidator.validateContentType(file, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateContentType_WithValidJpeg_ShouldReturnTrue() {
            // Given - JPEG magic number: FF D8 FF
            byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegHeader);

            // When
            boolean result = FileContentValidator.validateContentType(file, "image/jpeg");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateContentType_WithValidPng_ShouldReturnTrue() {
            // Given - PNG magic number: 89 50 4E 47
            byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

            // When
            boolean result = FileContentValidator.validateContentType(file, "image/png");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateContentType_WithValidGif_ShouldReturnTrue() {
            // Given - GIF magic number: 47 49 46 38
            byte[] gifHeader = new byte[]{'G', 'I', 'F', '8'};
            MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", gifHeader);

            // When
            boolean result = FileContentValidator.validateContentType(file, "image/gif");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateContentType_WithInvalidHeader_ShouldReturnFalse() {
            // Given
            byte[] invalidHeader = new byte[]{0x00, 0x00, 0x00, 0x00};
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", invalidHeader);

            // When
            boolean result = FileContentValidator.validateContentType(file, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateContentType_WithUnknownMimeType_ShouldReturnTrue() {
            // Given
            byte[] content = "test content".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content);

            // When
            boolean result = FileContentValidator.validateContentType(file, "text/plain");

            // Then
            assertThat(result).isTrue(); // Unknown types are skipped
        }

        @Test
        void validateContentType_WithEmptyFile_ShouldReturnFalse() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{});

            // When
            boolean result = FileContentValidator.validateContentType(file, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateContentType_WithTooSmallFile_ShouldReturnFalse() {
            // Given
            byte[] smallContent = new byte[]{(byte) 0xFF, (byte) 0xD8}; // Only 2 bytes
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", smallContent);

            // When
            boolean result = FileContentValidator.validateContentType(file, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class IsAllowedImageTypeTests {
        @Test
        void isAllowedImageType_WithValidJpeg_ShouldReturnTrue() {
            // Given
            byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegHeader);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/jpeg");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedImageType_WithValidPng_ShouldReturnTrue() {
            // Given
            byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/png");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedImageType_WithNonImageType_ShouldReturnFalse() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "text/plain");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void isAllowedImageType_WithNullContentType_ShouldReturnFalse() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", null, new byte[]{});

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void isAllowedImageType_WithDisallowedImageType_ShouldReturnFalse() {
            // Given - SVG is image/svg+xml but not in allowed list
            byte[] svgContent = "<svg></svg>".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.svg", "image/svg+xml", svgContent);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/svg+xml");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void isAllowedImageType_WithMismatchedHeader_ShouldReturnFalse() {
            // Given - JPEG content type but PNG header
            byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", pngHeader);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }
    }
}