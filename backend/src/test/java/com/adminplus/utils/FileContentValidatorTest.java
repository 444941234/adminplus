package com.adminplus.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileContentValidator 测试类
 * 使用 Apache Tika 进行文件类型检测
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
class FileContentValidatorTest {

    @Nested
    class DetectMimeTypeTests {
        @Test
        void detectMimeType_WithJpegFile_ShouldReturnImageJpeg() {
            // Given - JPEG magic number: FF D8 FF
            byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegHeader);

            // When
            String mimeType = FileContentValidator.detectMimeType(file);

            // Then
            assertThat(mimeType).isEqualTo("image/jpeg");
        }

        @Test
        void detectMimeType_WithPngFile_ShouldReturnImagePng() {
            // Given - PNG magic number: 89 50 4E 47 0D 0A 1A 0A
            byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
            MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

            // When
            String mimeType = FileContentValidator.detectMimeType(file);

            // Then
            assertThat(mimeType).isEqualTo("image/png");
        }

        @Test
        void detectMimeType_WithPdfFile_ShouldReturnApplicationPdf() {
            // Given - PDF starts with %PDF-
            byte[] pdfContent = "%PDF-1.4\n%test".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdfContent);

            // When
            String mimeType = FileContentValidator.detectMimeType(file);

            // Then
            assertThat(mimeType).isEqualTo("application/pdf");
        }

        @Test
        void detectMimeType_WithEmptyFile_ShouldReturnNull() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{});

            // When
            String mimeType = FileContentValidator.detectMimeType(file);

            // Then
            assertThat(mimeType).isNull();
        }

        @Test
        void detectMimeType_WithZipFile_ShouldReturnApplicationZip() {
            // Given - ZIP magic number: 50 4B
            byte[] zipHeader = new byte[]{0x50, 0x4B, 0x03, 0x04};
            MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", zipHeader);

            // When
            String mimeType = FileContentValidator.detectMimeType(file);

            // Then
            assertThat(mimeType).isEqualTo("application/zip");
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
            byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
            MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/png");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedImageType_WithValidGif_ShouldReturnTrue() {
            // Given - GIF87a or GIF89a
            byte[] gifHeader = "GIF87a".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", gifHeader);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/gif");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedImageType_WithNonImageType_ShouldReturnFalse() {
            // Given - PDF content
            byte[] pdfContent = "%PDF-1.4".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdfContent);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "application/pdf");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void isAllowedImageType_WithEmptyFile_ShouldReturnFalse() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{});

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void isAllowedImageType_WithSvg_ShouldReturnFalse() {
            // Given - SVG is not in allowed image types
            byte[] svgContent = "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.svg", "image/svg+xml", svgContent);

            // When
            boolean result = FileContentValidator.isAllowedImageType(file, "image/svg+xml");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class IsAllowedFileTypeTests {
        @Test
        void isAllowedFileType_WithValidPdf_ShouldReturnTrue() {
            // Given
            byte[] pdfContent = "%PDF-1.4\n%test content".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdfContent);

            // When
            boolean result = FileContentValidator.isAllowedFileType(file, "application/pdf");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedFileType_WithValidJpeg_ShouldReturnTrue() {
            // Given
            byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegHeader);

            // When
            boolean result = FileContentValidator.isAllowedFileType(file, "image/jpeg");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedFileType_WithValidPng_ShouldReturnTrue() {
            // Given
            byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
            MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngHeader);

            // When
            boolean result = FileContentValidator.isAllowedFileType(file, "image/png");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedFileType_WithZipFile_ShouldReturnTrue() {
            // Given
            byte[] zipHeader = new byte[]{0x50, 0x4B, 0x03, 0x04};
            MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", zipHeader);

            // When
            boolean result = FileContentValidator.isAllowedFileType(file, "application/zip");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedFileType_WithDocxFile_ShouldReturnTrue() {
            // Given - DOCX is ZIP format but with .docx extension
            byte[] zipHeader = new byte[]{0x50, 0x4B, 0x03, 0x04};
            MockMultipartFile file = new MockMultipartFile("file", "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", zipHeader);

            // When
            boolean result = FileContentValidator.isAllowedFileType(file,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void isAllowedFileType_WithEmptyFile_ShouldReturnFalse() {
            // Given
            MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[]{});

            // When
            boolean result = FileContentValidator.isAllowedFileType(file, "application/pdf");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void isAllowedFileType_WithDisallowedType_ShouldReturnFalse() {
            // Given - EXE file is not allowed
            byte[] exeHeader = new byte[]{0x4D, 0x5A}; // MZ header
            MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/octet-stream", exeHeader);

            // When
            boolean result = FileContentValidator.isAllowedFileType(file, "application/octet-stream");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class ValidateExtensionTests {
        @Test
        void validateExtension_WithMatchingJpegExtension_ShouldReturnTrue() {
            // When
            boolean result = FileContentValidator.validateExtension("test.jpg", "image/jpeg");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateExtension_WithMatchingJpegExtensionUpperCase_ShouldReturnTrue() {
            // When
            boolean result = FileContentValidator.validateExtension("TEST.JPG", "image/jpeg");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateExtension_WithMatchingPngExtension_ShouldReturnTrue() {
            // When
            boolean result = FileContentValidator.validateExtension("test.png", "image/png");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateExtension_WithMatchingPdfExtension_ShouldReturnTrue() {
            // When
            boolean result = FileContentValidator.validateExtension("document.pdf", "application/pdf");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateExtension_WithNonMatchingExtension_ShouldReturnFalse() {
            // When
            boolean result = FileContentValidator.validateExtension("test.txt", "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateExtension_WithNullFilename_ShouldReturnFalse() {
            // When
            boolean result = FileContentValidator.validateExtension(null, "image/jpeg");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateExtension_WithNullContentType_ShouldReturnFalse() {
            // When
            boolean result = FileContentValidator.validateExtension("test.jpg", null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void validateExtension_WithJpegAlternateExtension_ShouldReturnTrue() {
            // When
            boolean result = FileContentValidator.validateExtension("test.jpeg", "image/jpeg");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void validateExtension_WithZipExtensionForDocx_ShouldReturnTrue() {
            // When - ZIP MIME type allows docx extension
            boolean result = FileContentValidator.validateExtension("document.docx", "application/zip");

            // Then
            assertThat(result).isTrue();
        }
    }
}